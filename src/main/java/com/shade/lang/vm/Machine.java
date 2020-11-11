package com.shade.lang.vm;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.parser.Parser;
import com.shade.lang.parser.ScriptException;
import com.shade.lang.parser.Tokenizer;
import com.shade.lang.parser.node.Node;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.node.stmt.ImportStatement;
import com.shade.lang.parser.token.Region;
import com.shade.lang.vm.runtime.Class;
import com.shade.lang.vm.runtime.*;
import com.shade.lang.vm.runtime.function.Function;
import com.shade.lang.vm.runtime.function.NativeFunction;
import com.shade.lang.vm.runtime.function.RuntimeFunction;
import com.shade.lang.vm.runtime.value.NumberValue;
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

import static com.shade.lang.compiler.Opcode.*;

public class Machine {
    public static final int MAX_STACK_DEPTH = 8192;
    public static final int MAX_CODE_SIZE = 16384;

    private final List<Path> searchRoots = new ArrayList<>();
    private final Map<String, Module> modules = new HashMap<>();
    private final Stack<ScriptObject> operandStack = new Stack<>();
    private final Stack<Frame> callStack = new Stack<>();

    private PrintStream out = System.out;
    private PrintStream err = System.err;

    private boolean halted;
    private int status;

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

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            Parser parser = new Parser(new Tokenizer(reader));

            Node node = parser.parse(source, Parser.Mode.Unit);
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

            switch (frame.nextImm8()) {
                case PUSH_CONST: {
                    operandStack.push(Value.from(frame.nextConstant()));
                    break;
                }
                case PUSH_INT: {
                    operandStack.push(NumberValue.from(frame.nextImm32()));
                    break;
                }
                case GET_GLOBAL: {
                    Module module = frame.getFunction().getModule();
                    String name = frame.nextConstant();
                    ScriptObject value = module.getAttribute(name);
                    if (value == null) {
                        panic("No such global '" + name + "'", true);
                        break;
                    }
                    operandStack.push(value);
                    break;
                }
                case SET_GLOBAL: {
                    Module module = frame.getFunction().getModule();
                    module.setAttribute(frame.nextConstant(), operandStack.pop());
                    break;
                }
                case GET_LOCAL: {
                    operandStack.push(frame.locals[frame.nextImm8()]);
                    break;
                }
                case SET_LOCAL: {
                    frame.locals[frame.nextImm8()] = operandStack.pop();
                    break;
                }
                case GET_ATTRIBUTE: {
                    String name = frame.nextConstant();
                    ScriptObject target = operandStack.pop();
                    ScriptObject object = target.getAttribute(name);
                    if (object == null) {
                        panic("No such attribute '" + name + "' on target '" + target + "'", true);
                        break;
                    }
                    operandStack.push(object);
                    break;
                }
                case SET_ATTRIBUTE: {
                    ScriptObject value = operandStack.pop();
                    ScriptObject target = operandStack.pop();
                    target.setAttribute(frame.nextConstant(), value);
                    break;
                }
                case ADD: {
                    Value b = (Value) operandStack.pop();
                    Value a = (Value) operandStack.pop();
                    Value result = a.add(this, b);
                    if (result != null) {
                        operandStack.push(result);
                    }
                    break;
                }
                case SUB: {
                    Value b = (Value) operandStack.pop();
                    Value a = (Value) operandStack.pop();
                    Value result = a.sub(this, b);
                    if (result != null) {
                        operandStack.push(result);
                    }
                    break;
                }
                case MUL: {
                    Value b = (Value) operandStack.pop();
                    Value a = (Value) operandStack.pop();
                    Value result = a.mul(this, b);
                    if (result != null) {
                        operandStack.push(result);
                    }
                    break;
                }
                case DIV: {
                    Value b = (Value) operandStack.pop();
                    Value a = (Value) operandStack.pop();
                    Value result = a.div(this, b);
                    if (result != null) {
                        operandStack.push(result);
                    }
                    break;
                }
                case JUMP: {
                    frame.pc += frame.nextImm32();
                    break;
                }
                case JUMP_IF_TRUE: {
                    Boolean value = ((Value) operandStack.pop()).getBoolean(this);
                    int offset = frame.nextImm32();
                    if (value != null && value) {
                        frame.pc += offset - 4;
                    }
                    break;
                }
                case JUMP_IF_FALSE: {
                    Boolean value = ((Value) operandStack.pop()).getBoolean(this);
                    int offset = frame.nextImm32();
                    if (value != null && !value) {
                        frame.pc += offset - 4;
                    }
                    break;
                }
                case CMP_EQ: {
                    ScriptObject b = operandStack.pop();
                    ScriptObject a = operandStack.pop();
                    operandStack.push(NumberValue.from(a.equals(b) ? 1 : 0));
                    break;
                }
                case CMP_NE: {
                    ScriptObject b = operandStack.pop();
                    ScriptObject a = operandStack.pop();
                    operandStack.push(NumberValue.from(a.equals(b) ? 0 : 1));
                    break;
                }
                case CMP_LT: {
                    Value b = (Value) operandStack.pop();
                    Value a = (Value) operandStack.pop();
                    Integer result = a.compare(this, b);
                    if (result != null) {
                        operandStack.push(NumberValue.from(result < 0 ? 1 : 0));
                    }
                    break;
                }
                case CMP_LE: {
                    Value b = (Value) operandStack.pop();
                    Value a = (Value) operandStack.pop();
                    Integer result = a.compare(this, b);
                    if (result != null) {
                        operandStack.push(NumberValue.from(result <= 0 ? 1 : 0));
                    }
                    break;
                }
                case CMP_GT: {
                    Value b = (Value) operandStack.pop();
                    Value a = (Value) operandStack.pop();
                    Integer result = a.compare(this, b);
                    if (result != null) {
                        operandStack.push(NumberValue.from(result > 0 ? 1 : 0));
                    }
                    break;
                }
                case CMP_GE: {
                    Value b = (Value) operandStack.pop();
                    Value a = (Value) operandStack.pop();
                    Integer result = a.compare(this, b);
                    if (result != null) {
                        operandStack.push(NumberValue.from(result >= 0 ? 1 : 0));
                    }
                    break;
                }
                case CALL: {
                    byte argc = frame.nextImm8();
                    Object callable = operandStack.pop();
                    if (!(callable instanceof Function)) {
                        panic("Not a callable object: " + callable);
                        break;
                    }
                    Function function = (Function) callable;
                    function.invoke(this, argc);
                    break;
                }
                case RET: {
                    callStack.pop();
                    if (callStack.empty()) {
                        return;
                    }
                    break;
                }
                case POP: {
                    operandStack.pop();
                    break;
                }
                case DUP: {
                    operandStack.push(operandStack.peek());
                    break;
                }
                case DUP_AT: {
                    operandStack.push(operandStack.get(operandStack.size() + frame.nextImm8()));
                    break;
                }
                case BIND: {
                    ScriptObject value = operandStack.pop();
                    RuntimeFunction function = (RuntimeFunction) operandStack.pop();
                    function.getBoundArguments()[frame.nextImm8()] = value;
                    break;
                }
                case NOT: {
                    Integer value = (Integer) ((Value) operandStack.pop()).getValue();
                    operandStack.push(NumberValue.from(value == 0 ? 1 : 0));
                    break;
                }
                case ASSERT: {
                    Integer value = (Integer) ((Value) operandStack.pop()).getValue();
                    String source = frame.nextConstant();
                    String message = frame.nextConstant();
                    if (value == 0) {
                        if (message != null) {
                            panic("Assertion failed '" + source + "': " + message, true);
                        } else {
                            panic("Assertion failed '" + source + "'", true);
                        }
                    }
                    break;
                }
                case IMPORT: {
                    String name = frame.nextConstant();
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
                case NEW: {
                    ScriptObject object = operandStack.pop();
                    if (!(object instanceof Class)) {
                        panic("Cannot instantiate a non-class value");
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

        while (!callStack.empty()) {
            Frame currentFrame = callStack.peek();

            if (recoverable && currentFrame.getFunction() instanceof RuntimeFunction) {
                RuntimeFunction function = (RuntimeFunction) currentFrame.getFunction();

                for (Assembler.Guard guard : function.getGuards()) {
                    if (currentFrame.pc > guard.getStart() && currentFrame.pc <= guard.getEnd()) {
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
        private final String[] constants;
        private final ScriptObject[] locals;
        private int pc;

        public Frame(Function function, byte[] chunk, String[] constants, ScriptObject[] locals) {
            this.function = function;
            this.chunk = chunk;
            this.constants = constants;
            this.locals = locals;
            this.pc = 0;
        }

        private String nextConstant() {
            return constants[nextImm32()];
        }

        private int nextImm32() {
            return (chunk[pc++] & 0xff) << 24 | (chunk[pc++] & 0xff) << 16 | (chunk[pc++] & 0xff) << 8 | (chunk[pc++] & 0xff);
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

        public String[] getConstants() {
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
            return pc == frame.pc &&
                Objects.equals(function, frame.function) &&
                Arrays.equals(chunk, frame.chunk);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(function, pc);
            result = 31 * result + Arrays.hashCode(chunk);
            return result;
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
            super(null, null, null, null);
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
