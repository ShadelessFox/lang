package com.shade.lang.runtime;

import com.shade.lang.compiler.assembler.Operand;
import com.shade.lang.compiler.assembler.Operation;
import com.shade.lang.compiler.optimizer.Optimizer;
import com.shade.lang.compiler.parser.Parser;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.Tokenizer;
import com.shade.lang.compiler.parser.node.Node;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.runtime.frames.*;
import com.shade.lang.runtime.objects.Class;
import com.shade.lang.runtime.objects.*;
import com.shade.lang.runtime.objects.extension.Index;
import com.shade.lang.runtime.objects.extension.MutableIndex;
import com.shade.lang.runtime.objects.function.BoundFunction;
import com.shade.lang.runtime.objects.function.Function;
import com.shade.lang.runtime.objects.function.Guard;
import com.shade.lang.runtime.objects.function.RuntimeFunction;
import com.shade.lang.runtime.objects.module.Module;
import com.shade.lang.runtime.objects.value.NoneValue;
import com.shade.lang.runtime.objects.value.Value;
import com.shade.lang.util.annotations.NotNull;
import com.shade.lang.util.annotations.Nullable;
import com.shade.lang.util.serialization.ModuleSerializer;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.shade.lang.compiler.assembler.OperationCode.*;

public class Machine {
    public static final int MAX_STACK_DEPTH = 8192;
    public static final int MAX_STACK_REPETITIONS = 3;

    public static final boolean ENABLE_PROFILING = "true".equals(System.getProperty("ash.profiler.enable"));
    public static final boolean ENABLE_LOGGING = "true".equals(System.getProperty("ash.logging.enable"));
    public static final boolean ENABLE_CACHING = "true".equals(System.getProperty("ash.caching.enable"));

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

        if (ENABLE_CACHING) {
            final Path compiledPath = Paths.get(path.toString() + 'c');

            if (Files.exists(compiledPath)) {
                try (DataInputStream is = new DataInputStream(new FileInputStream(compiledPath.toFile()))) {
                    final Module module = ModuleSerializer.readModule(is, ModuleSerializer.readFileChecksum(path.toFile()));

                    if (module != null) {
                        LOG.info("Loading cached module '" + name + "' from '" + compiledPath + "'");
                        return load(module);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        Module module = new Module(name, source);
        Context context = new Context(module);

        LOG.info("Loading module '" + name + "' from '" + path + "'");

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            Parser parser = new Parser(new Tokenizer(reader));

            Node node = parser.parse(source, Parser.Mode.Unit);
            node = Optimizer.optimize(node, Integer.getInteger("ash.opt.level", Integer.MAX_VALUE), Integer.getInteger("ash.opt.passes", 0));
            node.compile(context, null);

            if (ENABLE_CACHING) {
                final Path compiledPath = Paths.get(path.toString() + 'c');

                try (DataOutputStream os = new DataOutputStream(new FileOutputStream(compiledPath.toFile()))) {
                    LOG.info("Caching module '" + name + "' from '" + path + "'");
                    ModuleSerializer.writeModule(os, module, ModuleSerializer.readFileChecksum(path.toFile()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            return load(module);
        } catch (ScriptException e) {
            callStack.push(new ParserFrame(module, source, e, operandStack.size()));
            panic(e.getMessage(), false);
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public Module load(@NotNull Module module) {
        if (modules.put(module.getName(), module) != null) {
            throw new IllegalArgumentException("Module already loaded: " + module.getName());
        }

        module.setAttribute("<builtin>", modules.get("builtin"));

        if (module.getChunk() != null) {
            callStack.push(new ModuleFrame(module, operandStack.size()));
            execute();
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

        panic("Cannot find module named '" + name + "'", true);

        return null;
    }

    @Nullable
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

        if (function instanceof RuntimeFunction) {
            return execute();
        }

        if (halted) {
            return null;
        }

        return operandStack.pop();
    }

    @Nullable
    private Object execute() {
        final Frame rootFrame = callStack.isEmpty() ? null : callStack.peek();

        while (!halted) {
            if (callStack.size() > MAX_STACK_DEPTH) {
                panic("Stack overflow", false);
                break;
            }

            final Frame frame = callStack.peek();
            final byte opcode = frame.getNextImm8();

            if (ENABLE_LOGGING) {
                final StringBuilder sb = new StringBuilder();
                sb.append("Dispatching (PC: ").append(frame.pc - 1).append(")\n");
                sb.append("Frame:  ").append(frame.getModule().getName());
                if (frame instanceof RuntimeFrame) {
                    sb.append('/').append(((RuntimeFrame) frame).getFunction().getName());
                } else if (frame instanceof NativeFrame) {
                    sb.append('/').append(((NativeFrame) frame).getFunction().getName());
                }
                sb.append('\n');
                sb.append("Opcode: ").append(opcode).append(" (").append(Operation.values()[opcode - 1]).append(")\n");
                sb.append("Stack:  ").append(operandStack);
                LOG.info(sb.toString());
            }

            dispatch:
            switch (opcode) {
                case OP_PUSH: {
                    operandStack.push(Value.from(frame.getNextConstant()));
                    break;
                }
                case OP_GET_GLOBAL: {
                    Module module = frame.getModule();
                    String name = (String) frame.getNextConstant();
                    ScriptObject value = module.getAttribute(name);
                    if (value == null) {
                        panic("Module '" + module.getName() + "' has no such global '" + name + "'", true);
                        break;
                    }
                    operandStack.push(value);
                    break;
                }
                case OP_SET_GLOBAL: {
                    Module module = frame.getModule();
                    module.setAttribute((String) frame.getNextConstant(), operandStack.pop());
                    break;
                }
                case OP_GET_LOCAL: {
                    operandStack.push(frame.getLocals()[frame.getNextImm8()]);
                    break;
                }
                case OP_SET_LOCAL: {
                    frame.getLocals()[frame.getNextImm8()] = operandStack.pop();
                    break;
                }
                case OP_GET_ATTRIBUTE: {
                    String name = (String) frame.getNextConstant();
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
                    String name = (String) frame.getNextConstant();
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
                    short offset = frame.getNextImm16();
                    frame.pc += offset;
                    break;
                }
                case OP_JUMP_IF_TRUE: {
                    Value value = (Value) operandStack.pop();
                    short offset = frame.getNextImm16();
                    if (value.getBoolean(this) == Boolean.TRUE) {
                        frame.pc += offset;
                    }
                    break;
                }
                case OP_JUMP_IF_FALSE: {
                    Value value = (Value) operandStack.pop();
                    int offset = frame.getNextImm16();
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
                    byte argc = frame.getNextImm8();
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
                    while (oldFrame.getStackSize() < operandStack.size() - 1) {
                        operandStack.removeElementAt(operandStack.size() - 2);
                    }
                    profilerEndFrame(oldFrame);
                    if (oldFrame == rootFrame) {
                        return operandStack.pop();
                    }
                    if (callStack.empty()) {
                        return null;
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
                    operandStack.push(operandStack.get(operandStack.size() + frame.getNextImm8()));
                    break;
                }
                case OP_BIND: {
                    final ScriptObject value = operandStack.pop();
                    final BoundFunction function = (BoundFunction) operandStack.pop();
                    function.getBoundArguments()[frame.getNextImm8()] = value;
                    break;
                }
                case OP_NOT: {
                    Value value = (Value) operandStack.pop();
                    operandStack.push(Value.from(value.getBoolean(this) == Boolean.FALSE));
                    break;
                }
                case OP_ASSERT: {
                    Value value = (Value) operandStack.pop();
                    String source = (String) frame.getNextConstant();
                    Object message = frame.getNextConstant();
                    if (value.getBoolean(this) == Boolean.FALSE) {
                        if (message == NoneValue.INSTANCE) {
                            panic("Assertion failed '" + source + "'", true);
                        } else {
                            panic("Assertion failed '" + source + "': " + message, true);
                        }
                    }
                    break;
                }
                case OP_IMPORT: {
                    final String name = (String) frame.getNextConstant();
                    final byte slot = frame.getNextImm8();
                    final Module module = load(name);
                    if (module != null) {
                        if (module == frame.getModule()) {
                            panic("Cannot import itself", true);
                            break;
                        }
                        if (slot != Operand.UNDEFINED) {
                            frame.getLocals()[slot] = module;
                        } else {
                            operandStack.push(module);
                        }
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
                case OP_SUPER: {
                    final ScriptObject instance = operandStack.pop();
                    final ScriptObject base = operandStack.pop();
                    if (!(instance instanceof Instance)) {
                        panic("Object '" + instance + "' must be class instance", true);
                        break;
                    }
                    if (!(base instanceof Class)) {
                        panic("Object '" + instance + "' must be class", true);
                        break;
                    }
                    if (!((Class) base).isInstance((Instance) instance)) {
                        panic("Object '" + instance + "' is not an instance of '" + base + "'", true);
                    }
                    operandStack.push(new Proxy((Instance) instance, (Class) base));
                    break;
                }
                case OP_INSTANCE_OF: {
                    final ScriptObject clazz = operandStack.pop();
                    final ScriptObject object = operandStack.pop();
                    if (!(object instanceof Instance)) {
                        panic("Left operand of an 'is' operator must be class instance", true);
                        break;
                    }
                    if (!(clazz instanceof Class)) {
                        panic("Right operand of an 'is' operator must be class", true);
                        break;
                    }
                    operandStack.push(Value.from(((Class) clazz).isInstance((Instance) object)));
                    break;
                }
                case OP_MAKE_FUNCTION: {
                    final String name = (String) frame.getNextConstant();
                    final Chunk chunk = (Chunk) frame.getNextConstant();
                    final Function function = new RuntimeFunction(frame.getModule(), name, chunk);

                    if (chunk.getBoundArguments() > 0) {
                        operandStack.push(new BoundFunction(function, function.getArity(), new ScriptObject[chunk.getBoundArguments()]));
                    } else {
                        operandStack.push(function);
                    }

                    break;
                }
                case OP_MAKE_CLASS: {
                    final String name = (String) frame.getNextConstant();
                    final Chunk chunk = (Chunk) frame.getNextConstant();
                    final Class[] bases = new Class[frame.getNextImm8()];

                    for (int index = bases.length - 1; index >= 0; index--) {
                        final ScriptObject object = operandStack.pop();

                        if (!(object instanceof Class)) {
                            panic("Class '" + name + "' cannot inherit from non-class object '" + object + "'", false);
                            break dispatch;
                        }

                        for (int nested = bases.length - 1; nested > index; nested--) {
                            if (object.equals(bases[nested])) {
                                panic("Class '" + name + "' has duplicated base class '" + ((Class) object).getName() + "'", false);
                                break dispatch;
                            }
                        }

                        bases[index] = (Class) object;
                    }

                    callStack.push(new ClassFrame(frame.getModule(), new Class(name, bases), chunk, operandStack.size()));
                    break;
                }
                case OP_THROW: {
                    final ScriptObject payload = operandStack.pop();
                    if (payload == NoneValue.INSTANCE) {
                        panic("Cannot throw 'none' as a payload", true);
                        break;
                    }
                    panic(payload, true);
                    break;
                }
                default:
                    panic(String.format("Not implemented opcode: %#04x", frame.getChunk().getCode()[frame.pc - 1]), false);
            }
        }

        return null;
    }

    public void panic(Object payload, boolean recoverable) {
        panic(Value.from(payload), recoverable);
    }

    public void panic(ScriptObject payload, boolean recoverable) {
        int lastFrameRepeated = 0;
        Frame lastFrame = null;

        StringBuilder builder = new StringBuilder();
        builder.append("Panicking: ").append(payload).append('\n');

        LOG.info("Panicking with payload '" + payload + "' in " + callStack.peek());

        while (!callStack.empty()) {
            Frame currentFrame = callStack.peek();

            while (currentFrame.getStackSize() < operandStack.size()) {
                operandStack.pop();
            }

            if (recoverable && currentFrame instanceof RuntimeFrame) {
                RuntimeFunction function = ((RuntimeFrame) currentFrame).getFunction();

                LOG.info("Checking for suitable guards in '" + function.getName() + "'...");

                for (Guard guard : function.getChunk().getGuards()) {
                    if (currentFrame.pc > guard.getStart() && currentFrame.pc <= guard.getEnd()) {
                        LOG.info("Got suitable guard, recovering");

                        if (guard.hasSlot()) {
                            currentFrame.getLocals()[guard.getSlot()] = payload;
                        }

                        currentFrame.pc = guard.getOffset();
                        return;
                    }
                }
            }

            if (currentFrame.equals(lastFrame)) {
                lastFrameRepeated++;
            }

            if (lastFrameRepeated < MAX_STACK_REPETITIONS) {
                builder.append("    at ").append(currentFrame).append('\n');
            }

            lastFrame = callStack.pop();
        }

        if (lastFrameRepeated > MAX_STACK_REPETITIONS) {
            builder.append("    [repeated ").append(lastFrameRepeated - MAX_STACK_REPETITIONS).append(" more time(-s)]").append('\n');
        }

        err.print(builder);

        halt(-1);
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
