package com.shade.lang.vm;

import com.shade.lang.compiler.Operation;
import com.shade.lang.optimizer.Optimizer;
import com.shade.lang.parser.Parser;
import com.shade.lang.parser.ScriptException;
import com.shade.lang.parser.Tokenizer;
import com.shade.lang.parser.node.Node;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.node.stmt.ImportStatement;
import com.shade.lang.parser.token.Region;
import com.shade.lang.vm.runtime.Class;
import com.shade.lang.vm.runtime.ScriptObject;
import com.shade.lang.vm.runtime.extension.Index;
import com.shade.lang.vm.runtime.extension.MutableIndex;
import com.shade.lang.vm.runtime.function.Function;
import com.shade.lang.vm.runtime.function.Guard;
import com.shade.lang.vm.runtime.function.NativeFunction;
import com.shade.lang.vm.runtime.function.RuntimeFunction;
import com.shade.lang.vm.runtime.module.Module;
import com.shade.lang.vm.runtime.value.Value;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.shade.lang.compiler.OperationCode.*;

public class Machine {
    public static final int MAX_STACK_DEPTH = 8192;

    public static final boolean ENABLE_PROFILING = "true".equals(System.getProperty("ash.profiler.enable"));
    public static final boolean ENABLE_LOGGING = "true".equals(System.getProperty("ash.logging.enable"));

    private static final Logger LOG = Logger.getLogger(Machine.class.getName());

    private final List<Path> searchRoots = new ArrayList<>();
    private final Map<String, Module> modules = new HashMap<>();
    private final Stack<ScriptObject> operandStack = new Stack<>();
    private final Stack<Frame> callStack = new Stack<>();
    private final Map<Frame, List<Long>> profiler = new LinkedHashMap<>();
    private final Map<Frame, Long> profilerCache = new IdentityHashMap<>();

    private PrintStream out = System.out;
    private PrintStream err = System.err;

    private boolean halted;
    private int status;

    static {
        LOG.setLevel(Machine.ENABLE_LOGGING ? null : Level.OFF);
    }

    public Module load(Path path) {
        String source = path.toAbsolutePath().toString();
        String name = path.getFileName().toString();

        if (name.indexOf('.') >= 0) {
            name = name.substring(0, name.indexOf('.'));
        }

        if (modules.containsKey(name)) {
            return modules.get(name);
        }

        Module module = new Module(name, source);
        Context context = new Context(module);

        LOG.info("Loading module from '" + path + "'");

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            Parser parser = new Parser(new Tokenizer(reader));

            Node node = parser.parse(source, Parser.Mode.Unit);
            node = Optimizer.optimize(node, Integer.MAX_VALUE, Integer.getInteger("ash.optlevel", 0));
            node.compile(context, null);

            return load(module);
        } catch (ScriptException e) {
            callStack.push(new ParserFrame(source, e));
            panic(e.getMessage());
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Module load(Module module) throws ScriptException {
        Objects.requireNonNull(module);

        if (modules.containsKey(module.getName())) {
            throw new RuntimeException("Module already loaded: " + module.getName());
        }

        modules.put(module.getName(), module);

        for (ImportStatement statement : module.getImports()) {
            String name = statement.getName();
            String alias = statement.getAlias() == null ? name : statement.getAlias();
            Module loaded = load(name);
            if (loaded != null) {
                if (loaded == module) {
                    throw new ScriptException("Cannot import itself", statement.getRegion());
                }
                module.setAttribute(alias, loaded);
            } else {
                throw new ScriptException("Cannot find module named '" + name + "'", statement.getRegion());
            }
        }

        module.setAttribute("<builtin>", modules.get("builtin"));

        return module;
    }

    public Module load(String name) {
        Module module = modules.get(name);

        if (module != null) {
            return module;
        }

        for (Path root : searchRoots) {
            ImportFileVisitor visitor = new ImportFileVisitor(name);

            try {
                Files.walkFileTree(root, visitor);
            } catch (IOException e) {
                throw new RuntimeException("Internal error while loading module", e);
            }

            Path result = visitor.getResult();

            if (result != null) {
                return load(result);
            }
        }

        return null;
    }

    public Object call(String moduleName, String attributeName, Object... args) {
        Module module = modules.get(moduleName);

        if (module == null) {
            throw new RuntimeException("No such module: " + moduleName);
        }

        ScriptObject attribute = module.getAttribute(attributeName);

        if (attribute == null) {
            throw new RuntimeException("No such attribute: " + attributeName);
        }

        if (!(attribute instanceof Function)) {
            throw new RuntimeException("Attribute is not callable: " + attributeName);
        }

        for (Object arg : args) {
            if (arg instanceof ScriptObject) {
                operandStack.push((ScriptObject) arg);
            } else {
                operandStack.push(Value.from(arg));
            }
        }

        Function function = (Function) attribute;
        function.invoke(this, args.length);

        if (!(function instanceof NativeFunction)) {
            execute();
        }

        if (halted) {
            return null;
        }

        return operandStack.pop();
    }

    public void execute() {
        while (!halted) {
            if (callStack.size() > MAX_STACK_DEPTH) {
                panic("Stack overflow");
                break;
            }

            final Frame frame = callStack.peek();
            final byte opcode = frame.nextImm8();

            if (ENABLE_LOGGING) {
                LOG.info("Dispatching (PC: " + (frame.pc - 1) + ")\n" +
                    "Frame:  " + frame.getFunction().getModule().getName() + '/' + frame.getFunction().getName() + '\n' +
                    "Opcode: " + opcode + " (" + Operation.values()[opcode - 1] + ")\n" +
                    "Stack:  " + operandStack);
            }

            switch (opcode) {
                case OP_PUSH: {
                    operandStack.push(Value.from(frame.nextConstant()));
                    break;
                }
                case OP_GET_GLOBAL: {
                    Module module = frame.getFunction().getModule();
                    String name = (String) frame.nextConstant();
                    ScriptObject value = module.getAttribute(name);
                    if (value == null) {
                        panic("Module '" + module.getName() + "' has no such global '" + name + "'", true);
                        break;
                    }
                    operandStack.push(value);
                    break;
                }
                case OP_SET_GLOBAL: {
                    Module module = frame.getFunction().getModule();
                    module.setAttribute((String) frame.nextConstant(), operandStack.pop());
                    break;
                }
                case OP_GET_LOCAL: {
                    operandStack.push(frame.locals[frame.nextImm8()]);
                    break;
                }
                case OP_SET_LOCAL: {
                    frame.locals[frame.nextImm8()] = operandStack.pop();
                    break;
                }
                case OP_GET_ATTRIBUTE: {
                    String name = (String) frame.nextConstant();
                    ScriptObject target = operandStack.pop();
                    ScriptObject object = target.getAttribute(name);
                    if (object == null) {
                        panic("Object '" + target + "' has no such attribute '" + name + "'", true);
                        break;
                    }
                    operandStack.push(object);
                    break;
                }
                case OP_SET_ATTRIBUTE: {
                    ScriptObject value = operandStack.pop();
                    ScriptObject target = operandStack.pop();
                    String name = (String) frame.nextConstant();
                    if (target.isImmutable()) {
                        panic("Cannot assign attribute to immutable object '" + target + "'", true);
                        break;
                    }
                    target.setAttribute(name, value);
                    break;
                }
                case OP_GET_INDEX: {
                    ScriptObject index = operandStack.pop();
                    ScriptObject object = operandStack.pop();
                    if (!(object instanceof Index)) {
                        panic("Object '" + object + "' does not support index accessing", true);
                        break;
                    }
                    ScriptObject result = ((Index) object).getIndex(this, index);
                    if (result != null) {
                        operandStack.push(result);
                    }
                    break;
                }
                case OP_SET_INDEX: {
                    ScriptObject value = operandStack.pop();
                    ScriptObject index = operandStack.pop();
                    ScriptObject object = operandStack.pop();
                    if (!(object instanceof MutableIndex)) {
                        panic("Object '" + object + "' does not support index assignment", true);
                        break;
                    }
                    if (object.isImmutable()) {
                        panic("Cannot assign index to immutable object '" + object + "'", true);
                        break;
                    }
                    ((MutableIndex) object).setIndex(this, index, value);
                    break;
                }
                case OP_ADD: {
                    Value b = (Value) operandStack.pop();
                    Value a = (Value) operandStack.pop();
                    Value result = a.add(this, b);
                    if (result != null) {
                        operandStack.push(result);
                    }
                    break;
                }
                case OP_SUB: {
                    Value b = (Value) operandStack.pop();
                    Value a = (Value) operandStack.pop();
                    Value result = a.sub(this, b);
                    if (result != null) {
                        operandStack.push(result);
                    }
                    break;
                }
                case OP_MUL: {
                    Value b = (Value) operandStack.pop();
                    Value a = (Value) operandStack.pop();
                    Value result = a.mul(this, b);
                    if (result != null) {
                        operandStack.push(result);
                    }
                    break;
                }
                case OP_DIV: {
                    Value b = (Value) operandStack.pop();
                    Value a = (Value) operandStack.pop();
                    Value result = a.div(this, b);
                    if (result != null) {
                        operandStack.push(result);
                    }
                    break;
                }
                case OP_JUMP: {
                    short offset = frame.nextImm16();
                    frame.pc += offset;
                    break;
                }
                case OP_JUMP_IF_TRUE: {
                    Value value = (Value) operandStack.pop();
                    short offset = frame.nextImm16();
                    if (value.getBoolean(this) == Boolean.TRUE) {
                        frame.pc += offset;
                    }
                    break;
                }
                case OP_JUMP_IF_FALSE: {
                    Value value = (Value) operandStack.pop();
                    int offset = frame.nextImm16();
                    if (value.getBoolean(this) == Boolean.FALSE) {
                        frame.pc += offset;
                    }
                    break;
                }
                case OP_CMP_EQ: {
                    ScriptObject b = operandStack.pop();
                    ScriptObject a = operandStack.pop();
                    operandStack.push(Value.from(a.equals(b)));
                    break;
                }
                case OP_CMP_NE: {
                    ScriptObject b = operandStack.pop();
                    ScriptObject a = operandStack.pop();
                    operandStack.push(Value.from(!a.equals(b)));
                    break;
                }
                case OP_CMP_LT: {
                    Value b = (Value) operandStack.pop();
                    Value a = (Value) operandStack.pop();
                    Integer result = a.compare(this, b);
                    if (result != null) {
                        operandStack.push(Value.from(result < 0));
                    }
                    break;
                }
                case OP_CMP_LE: {
                    Value b = (Value) operandStack.pop();
                    Value a = (Value) operandStack.pop();
                    Integer result = a.compare(this, b);
                    if (result != null) {
                        operandStack.push(Value.from(result <= 0));
                    }
                    break;
                }
                case OP_CMP_GT: {
                    Value b = (Value) operandStack.pop();
                    Value a = (Value) operandStack.pop();
                    Integer result = a.compare(this, b);
                    if (result != null) {
                        operandStack.push(Value.from(result > 0));
                    }
                    break;
                }
                case OP_CMP_GE: {
                    Value b = (Value) operandStack.pop();
                    Value a = (Value) operandStack.pop();
                    Integer result = a.compare(this, b);
                    if (result != null) {
                        operandStack.push(Value.from(result >= 0));
                    }
                    break;
                }
                case OP_CALL: {
                    byte argc = frame.nextImm8();
                    Object object = operandStack.pop();
                    if (!(object instanceof Function)) {
                        panic("Object '" + object + "' is not callable", true);
                        break;
                    }
                    Function function = (Function) object;
                    function.invoke(this, argc);
                    break;
                }
                case OP_RETURN: {
                    final Frame oldFrame = callStack.pop();
                    while (oldFrame.sp < operandStack.size() - 1) {
                        operandStack.removeElementAt(operandStack.size() - 2);
                    }
                    profilerEndFrame(oldFrame);
                    if (callStack.empty()) {
                        return;
                    }
                    break;
                }
                case OP_POP: {
                    operandStack.pop();
                    break;
                }
                case OP_DUP: {
                    operandStack.push(operandStack.peek());
                    break;
                }
                case OP_DUP_AT: {
                    operandStack.push(operandStack.get(operandStack.size() + frame.nextImm8()));
                    break;
                }
                case OP_BIND: {
                    ScriptObject value = operandStack.pop();
                    RuntimeFunction function = (RuntimeFunction) operandStack.pop();
                    function.getBoundArguments()[frame.nextImm8()] = value;
                    break;
                }
                case OP_NOT: {
                    Value value = (Value) operandStack.pop();
                    operandStack.push(Value.from(value.getBoolean(this) == Boolean.FALSE));
                    break;
                }
                case OP_ASSERT: {
                    Value value = (Value) operandStack.pop();
                    String source = (String) frame.nextConstant();
                    String message = (String) frame.nextConstant();
                    if (value.getBoolean(this) == Boolean.FALSE) {
                        if (message != null) {
                            panic("Assertion failed '" + source + "': " + message, true);
                        } else {
                            panic("Assertion failed '" + source + "'", true);
                        }
                    }
                    break;
                }
                case OP_IMPORT: {
                    String name = (String) frame.nextConstant();
                    byte slot = frame.nextImm8();
                    Module module = load(name);
                    if (module != null) {
                        if (module == frame.getFunction().getModule()) {
                            panic("Cannot import itself", true);
                            break;
                        }
                        frame.locals[slot] = module;
                    } else {
                        panic("Cannot find module named '" + name + "'", true);
                    }
                    break;
                }
                case OP_NEW: {
                    ScriptObject object = operandStack.pop();
                    if (!(object instanceof Class)) {
                        panic("Cannot instantiate a non-class value", true);
                        break;
                    }
                    Class clazz = (Class) object;
                    operandStack.push(clazz.instantiate());
                    break;
                }
                default:
                    panic(String.format("Not implemented opcode: %#04x", frame.chunk[frame.pc - 1]));
            }
        }
    }

    public void panic(String message, boolean recoverable) {
        int lastFrameRepeated = 0;
        Frame lastFrame = null;

        StringBuilder builder = new StringBuilder();
        builder.append("Panicking: ").append(message).append('\n');

        LOG.info("Panicking with message '" + message + "' in " + callStack.peek());

        while (!callStack.empty()) {
            Frame currentFrame = callStack.peek();

            while (currentFrame.sp < operandStack.size()) {
                operandStack.pop();
            }

            if (recoverable && currentFrame.getFunction() instanceof RuntimeFunction) {
                RuntimeFunction function = (RuntimeFunction) currentFrame.getFunction();

                LOG.info("Checking for suitable guards in '" + function.getName() + "'...");

                for (Guard guard : function.getGuards()) {
                    if (currentFrame.pc > guard.getStart() && currentFrame.pc <= guard.getEnd()) {
                        LOG.info("Got suitable guard, recovering");

                        if (guard.hasSlot()) {
                            currentFrame.locals[guard.getSlot()] = Value.from(message);
                        }

                        currentFrame.pc = guard.getOffset();
                        return;
                    }
                }
            }

            if (currentFrame.equals(lastFrame)) {
                lastFrameRepeated++;
            }

            if (lastFrameRepeated < 3) {
                builder.append("    at ").append(currentFrame).append('\n');
            }

            lastFrame = callStack.pop();
        }

        if (lastFrameRepeated > 0) {
            builder.append("    [repeated ").append(lastFrameRepeated).append(" more time(-s)]").append('\n');
        }

        err.print(builder);

        halt(-1);
    }

    public void panic(String message) {
        panic(message, false);
    }

    public void halt(int status) {
        this.halted = true;
        this.status = status;
        LOG.info("Halting with status " + status);
    }

    public Map<Frame, List<Long>> getProfilerSamples() {
        return profiler;
    }

    public void profilerBeginFrame(Frame frame) {
        if (ENABLE_PROFILING) {
            profilerCache.put(frame, System.nanoTime());
        }
    }

    public void profilerEndFrame(Frame frame) {
        if (!ENABLE_PROFILING) {
            return;
        }

        long time = System.nanoTime();

        if (!profilerCache.containsKey(frame)) {
            LOG.warning("Invalid record for frame " + frame);
            return;
        }

        profiler
            .computeIfAbsent(frame, x -> new ArrayList<>())
            .add(time - profilerCache.remove(frame));
    }

    public Stack<ScriptObject> getOperandStack() {
        return operandStack;
    }

    public Stack<Frame> getCallStack() {
        return callStack;
    }

    public PrintStream getOut() {
        return out;
    }

    public void setOut(PrintStream out) {
        this.out = out;
    }

    public PrintStream getErr() {
        return err;
    }

    public void setErr(PrintStream err) {
        this.err = err;
    }

    public boolean isHalted() {
        return halted;
    }

    public int getStatus() {
        return status;
    }

    public Map<String, Module> getModules() {
        return modules;
    }

    public List<Path> getSearchRoots() {
        return searchRoots;
    }

    public static class Frame {
        private final Function function;
        private final byte[] chunk;
        private final Object[] constants;
        private final ScriptObject[] locals;
        private final int sp;
        private int pc;

        public Frame(Function function, byte[] chunk, Object[] constants, ScriptObject[] locals, int sp) {
            this.function = function;
            this.chunk = chunk;
            this.constants = constants;
            this.locals = locals;
            this.sp = sp;
            this.pc = 0;
        }

        private Object nextConstant() {
            return constants[nextImm16()];
        }

        private int nextImm32() {
            return (chunk[pc++] & 0xff) << 24 | (chunk[pc++] & 0xff) << 16 | (chunk[pc++] & 0xff) << 8 | (chunk[pc++] & 0xff);
        }

        private short nextImm16() {
            return (short) ((chunk[pc++] & 0xff) << 8 | (chunk[pc++] & 0xff));
        }

        private byte nextImm8() {
            return chunk[pc++];
        }

        public Function getFunction() {
            return function;
        }

        public byte[] getChunk() {
            return chunk;
        }

        public Object[] getConstants() {
            return constants;
        }

        public ScriptObject[] getLocals() {
            return locals;
        }

        public String getSourceLocation() {
            if (function instanceof RuntimeFunction) {
                Map<Integer, Region.Span> lines = ((RuntimeFunction) function).getLines();
                if (lines.containsKey(pc)) {
                    return function.getModule().getSource() + ':' + lines.get(pc);
                } else {
                    return function.getModule().getSource() + ':' + '+' + pc;
                }
            } else if (function instanceof NativeFunction) {
                return "Native function";
            } else {
                return "Unknown source";
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Frame frame = (Frame) o;
            return function.equals(frame.function);
        }

        @Override
        public int hashCode() {
            return Objects.hash(function);
        }

        @Override
        public String toString() {
            return function.getModule().getName() + '/' + function.getName() + '(' + getSourceLocation() + ')';
        }
    }

    private static class ParserFrame extends Frame {
        private final String source;
        private final ScriptException exception;

        public ParserFrame(String source, ScriptException exception) {
            super(null, null, null, null, 0);
            this.source = source;
            this.exception = exception;
        }

        @Override
        public String toString() {
            return source + "(" + exception.getRegion().getBegin() + ")";
        }
    }

    private static class FileVisitor extends SimpleFileVisitor<Path> {
        private final Path rootPath;
        private final Path filePath;
        private Path result;

        public FileVisitor(Path rootPath, Path filePath) {
            this.rootPath = rootPath;
            this.filePath = filePath;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            if (rootPath.relativize(file).equals(filePath)) {
                result = file;
                return FileVisitResult.TERMINATE;
            }

            return FileVisitResult.CONTINUE;
        }

        public Path getResult() {
            return result;
        }
    }

    private static class ImportFileVisitor extends SimpleFileVisitor<Path> {
        private final String name;
        private Path result;

        public ImportFileVisitor(String name) {
            this.name = name;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            String filename = file.getFileName().toString();

            if (filename.indexOf('.') >= 0) {
                filename = filename.substring(0, filename.indexOf('.'));
            }

            if (filename.equals(name)) {
                result = file;
                return FileVisitResult.TERMINATE;
            }

            return FileVisitResult.CONTINUE;
        }

        public Path getResult() {
            return result;
        }
    }
}
