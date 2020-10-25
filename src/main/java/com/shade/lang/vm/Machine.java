package com.shade.lang.vm;

import com.shade.lang.parser.Parser;
import com.shade.lang.parser.ScriptException;
import com.shade.lang.parser.Tokenizer;
import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.node.Node;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.node.stmt.ImportStatement;
import com.shade.lang.parser.token.Region;
import com.shade.lang.vm.runtime.Module;
import com.shade.lang.vm.runtime.ScriptObject;
import com.shade.lang.vm.runtime.Value;
import com.shade.lang.vm.runtime.function.AbstractFunction;
import com.shade.lang.vm.runtime.function.Function;
import com.shade.lang.vm.runtime.function.NativeFunction;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

import static com.shade.lang.parser.gen.Opcode.*;

public class Machine {
    public static final int MAX_STACK_DEPTH = 8192;
    public static final int MAX_CODE_SIZE = 16384;

    private final Map<String, Module> modules = new HashMap<>();
    private final Stack<ScriptObject> operandStack = new Stack<>();
    private final Stack<Frame> callStack = new Stack<>();

    private PrintStream out = System.out;
    private PrintStream err = System.err;

    private boolean halted;
    private int status;

    public void load(Module module) throws ScriptException {
        Objects.requireNonNull(module);

        if (modules.containsKey(module.getName())) {
            throw new RuntimeException("Module already loaded: " + module.getName());
        }

        modules.put(module.getName(), module);

        for (ImportStatement statement : module.getImports()) {
            if (statement.isPath()) {
                throw new ScriptException("Loading modules from file is not supported yet", statement.getRegion());
            }

            String name = statement.getName();

            if (modules.containsKey(name)) {
                String alias = statement.getAlias() == null ? name : statement.getAlias();
                module.setAttribute(alias, modules.get(name));
            } else {
                throw new ScriptException("No such module named '" + statement.getName() + "'", statement.getRegion());
            }
        }

        module.getImports().clear();
    }

    public void load(String name, String source, Reader reader) throws IOException {
        Module module = new Module(name, source);
        Context context = new Context(module, 0);

        try {
            Parser parser = new Parser(new Tokenizer(reader));
            Node node = parser.parse(source, Parser.Mode.Unit);
            node.compile(context, null);
            load(module);
        } catch (ScriptException e) {
            callStack.push(new ParserFrame(source, e));
            panic(e.getMessage());
            callStack.pop();
        }
    }

    public void load(String name, File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            load(name, file.getPath(), reader);
        }
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

        if (!(attribute instanceof AbstractFunction)) {
            throw new RuntimeException("Attribute is not callable: " + attributeName);
        }

        for (Object arg : args) {
            if (arg instanceof ScriptObject) {
                operandStack.push((ScriptObject) arg);
            } else {
                operandStack.push(new Value(arg));
            }
        }

        AbstractFunction function = (AbstractFunction) attribute;
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
                    operandStack.push(new Value(frame.nextConstant()));
                    break;
                }
                case PUSH_INT: {
                    operandStack.push(new Value(frame.nextImm32()));
                    break;
                }
                case GET_GLOBAL: {
                    Module module = frame.getFunction().getModule();
                    String name = frame.nextConstant();
                    ScriptObject value = module.getAttribute(name);
                    if (value == null) {
                        panic("No such global '" + name + "'");
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
                    operandStack.push(frame.slots[frame.nextImm8()]);
                    break;
                }
                case SET_LOCAL: {
                    frame.slots[frame.nextImm8()] = operandStack.pop();
                    break;
                }
                case GET_ATTRIBUTE: {
                    String name = frame.nextConstant();
                    ScriptObject target = operandStack.pop();
                    ScriptObject object = target.getAttribute(name);
                    if (object == null) {
                        panic("No such property '" + name + "' on target '" + target + "'");
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
                    Object b = ((Value) operandStack.pop()).getValue();
                    Object a = ((Value) operandStack.pop()).getValue();

                    if (a instanceof Integer && b instanceof Integer) {
                        operandStack.push(new Value((int) a + (int) b));
                    } else {
                        operandStack.push(new Value(a.toString() + b.toString()));
                    }

                    break;
                }
                case SUB: {
                    int b = (int) ((Value) operandStack.pop()).getValue();
                    int a = (int) ((Value) operandStack.pop()).getValue();
                    operandStack.push(new Value(a - b));
                    break;
                }
                case MUL: {
                    int b = (int) ((Value) operandStack.pop()).getValue();
                    int a = (int) ((Value) operandStack.pop()).getValue();
                    operandStack.push(new Value(a * b));
                    break;
                }
                case DIV: {
                    int b = (int) ((Value) operandStack.pop()).getValue();
                    int a = (int) ((Value) operandStack.pop()).getValue();
                    operandStack.push(new Value(a / b));
                    break;
                }
                case JUMP: {
                    frame.pc += frame.nextImm32();
                    break;
                }
                case JUMP_IF_TRUE: {
                    int value = (int) ((Value) operandStack.pop()).getValue();
                    int offset = frame.nextImm32();
                    if (value != 0) {
                        frame.pc += offset - 4;
                    }
                    break;
                }
                case JUMP_IF_FALSE: {
                    int value = (int) ((Value) operandStack.pop()).getValue();
                    int offset = frame.nextImm32();
                    if (value == 0) {
                        frame.pc += offset - 4;
                    }
                    break;
                }
                case CMP_EQ: {
                    Object b = ((Value) operandStack.pop()).getValue();
                    Object a = ((Value) operandStack.pop()).getValue();
                    operandStack.push(new Value(a.equals(b) ? 1 : 0));
                    break;
                }
                case CMP_NE: {
                    Object b = ((Value) operandStack.pop()).getValue();
                    Object a = ((Value) operandStack.pop()).getValue();
                    operandStack.push(new Value(a.equals(b) ? 0 : 1));
                    break;
                }
                case CMP_LT: {
                    int b = (int) ((Value) operandStack.pop()).getValue();
                    int a = (int) ((Value) operandStack.pop()).getValue();
                    operandStack.push(new Value(a < b ? 1 : 0));
                    break;
                }
                case CMP_LE: {
                    int b = (int) ((Value) operandStack.pop()).getValue();
                    int a = (int) ((Value) operandStack.pop()).getValue();
                    operandStack.push(new Value(a <= b ? 1 : 0));
                    break;
                }
                case CMP_GT: {
                    int b = (int) ((Value) operandStack.pop()).getValue();
                    int a = (int) ((Value) operandStack.pop()).getValue();
                    operandStack.push(new Value(a > b ? 1 : 0));
                    break;
                }
                case CMP_GE: {
                    int b = (int) ((Value) operandStack.pop()).getValue();
                    int a = (int) ((Value) operandStack.pop()).getValue();
                    operandStack.push(new Value(a >= b ? 1 : 0));
                    break;
                }
                case CALL: {
                    byte argc = frame.nextImm8();
                    Object callable = operandStack.pop();
                    if (!(callable instanceof AbstractFunction)) {
                        panic("Not a callable object: " + callable);
                        break;
                    }
                    AbstractFunction function = (AbstractFunction) callable;
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
                case NOT: {
                    int value = (int) ((Value) operandStack.pop()).getValue();
                    operandStack.push(new Value(value == 0 ? 1 : 0));
                    break;
                }
                case ASSERT: {
                    int value = (int) ((Value) operandStack.pop()).getValue();
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

            if (recoverable && currentFrame.getFunction() instanceof Function) {
                Function function = (Function) currentFrame.getFunction();

                for (Assembler.Guard guard : function.getGuards()) {
                    if (currentFrame.pc > guard.getStart() && currentFrame.pc <= guard.getEnd()) {
                        if (guard.hasSlot()) {
                            currentFrame.slots[guard.getSlot()] = new Value(message);
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

    public static class Frame {
        private final AbstractFunction function;
        private final byte[] chunk;
        private final String[] constants;
        private final ScriptObject[] slots;
        private final Map<Integer, Region.Span> lines;
        private int pc;

        public Frame(AbstractFunction function, byte[] chunk, String[] constants, ScriptObject[] slots, Map<Integer, Region.Span> lines) {
            this.function = function;
            this.chunk = chunk;
            this.constants = constants;
            this.slots = slots;
            this.lines = lines;
            this.pc = 0;
        }

        public AbstractFunction getFunction() {
            return function;
        }

        @Override
        public String toString() {
            String position = lines.containsKey(pc) ? lines.get(pc).toString() : "+" + pc;
            return function.getModule().getName() + "/" + function.getName() + "(" + function.getModule().getSource() + ":" + position + ")";
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Frame frame = (Frame) o;
            return pc == frame.pc &&
                function.equals(frame.function);
        }

        @Override
        public int hashCode() {
            return Objects.hash(function, pc);
        }
    }

    public static class NativeFrame extends Frame {
        public NativeFrame(NativeFunction function) {
            super(function, null, null, null, null);
        }

        @Override
        public String toString() {
            return getFunction().getModule().getName() + "/" + getFunction().getName() + "(Native Method)";
        }
    }

    public static class ParserFrame extends Frame {
        private final String source;
        private final ScriptException exception;

        public ParserFrame(String source, ScriptException exception) {
            super(null, null, null, null, null);
            this.source = source;
            this.exception = exception;
        }

        @Override
        public String toString() {
            return source + "(" + exception.getRegion().getBegin() + ")";
        }
    }
}
