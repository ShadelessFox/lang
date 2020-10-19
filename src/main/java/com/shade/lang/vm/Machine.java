package com.shade.lang.vm;

import com.shade.lang.vm.runtime.Module;
import com.shade.lang.vm.runtime.ScriptObject;
import com.shade.lang.vm.runtime.function.AbstractFunction;
import com.shade.lang.vm.runtime.function.NativeFunction;

import java.util.*;

import static com.shade.lang.parser.gen.Opcode.*;

public class Machine {
    public static final int MAX_STACK_DEPTH = 8192;
    public static final int MAX_CODE_SIZE = 16384;

    private final Map<String, Module> modules = new HashMap<>();
    private final Stack<Object> operandStack = new Stack<>();
    private final Stack<Frame> callStack = new Stack<>();

    private boolean halted;

    public void load(Module module) {
        Objects.requireNonNull(module);

        if (modules.containsKey(module.getName())) {
            throw new RuntimeException("Module already loaded: " + module.getName());
        }

        modules.put(module.getName(), module);
    }

    public Object call(String moduleName, String attributeName, Object... args) {
        Module module = modules.get(moduleName);

        if (module == null) {
            throw new RuntimeException("No such module: " + moduleName);
        }

        ScriptObject attribute = module.getAttributes().get(attributeName);

        if (attribute == null) {
            throw new RuntimeException("No such attribute: " + attributeName);
        }

        if (!(attribute instanceof AbstractFunction)) {
            throw new RuntimeException("Attribute is not callable: " + attributeName);
        }

        for (Object arg : args) {
            operandStack.push(arg);
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
                case PUSH_INT: {
                    operandStack.push(frame.nextImm32());
                    break;
                }
                case GET_GLOBAL: {
                    String name = frame.nextConstant();
                    ScriptObject object = modules.get(name);
                    if (object == null) {
                        panic("No such global '" + name + "'");
                        break;
                    }
                    operandStack.push(object);
                    break;
                }
                case GET_ATTRIBUTE: {
                    String name = frame.nextConstant();
                    ScriptObject target = (ScriptObject) operandStack.pop();
                    ScriptObject object = target.getAttributes().get(name);
                    if (object == null) {
                        panic("No such property '" + name + "' on target '" + target + "'");
                        break;
                    }
                    operandStack.push(object);
                    break;
                }
                case SET_ATTRIBUTE: {
                    ScriptObject value = (ScriptObject) operandStack.pop();
                    ScriptObject target = (ScriptObject) operandStack.pop();
                    target.getAttributes().put(frame.nextConstant(), value);
                    break;
                }
                case ADD: {
                    int b = (int) operandStack.pop();
                    int a = (int) operandStack.pop();
                    operandStack.push(a + b);
                    break;
                }
                case SUB: {
                    int b = (int) operandStack.pop();
                    int a = (int) operandStack.pop();
                    operandStack.push(a - b);
                    break;
                }
                case MUL: {
                    int b = (int) operandStack.pop();
                    int a = (int) operandStack.pop();
                    operandStack.push(a * b);
                    break;
                }
                case DIV: {
                    int b = (int) operandStack.pop();
                    int a = (int) operandStack.pop();
                    operandStack.push(a / b);
                    break;
                }
                case CALL: {
                    Object callable = operandStack.pop();
                    if (!(callable instanceof AbstractFunction)) {
                        panic("Not a callable object: " + callable);
                        break;
                    }
                    AbstractFunction function = (AbstractFunction) callable;
                    function.invoke(this, frame.nextImm8());
                    break;
                }
                case RET: {
                    callStack.pop();
                    if (callStack.empty()) {
                        return;
                    }
                    callStack.peek().pc -= 1;
                    break;
                }
                case POP: {
                    operandStack.pop();
                    break;
                }
                default:
                    panic("Not implemented opcode: " + frame.chunk[frame.pc - 1]);
            }
        }
    }

    public void panic(String message) {
        int lastFrameRepeated = 0;
        Frame lastFrame = null;

        System.err.println("Panicking: " + message);

        for (int index = callStack.size(); index > 0; index--) {
            Frame currentFrame = callStack.get(index - 1);

            if (currentFrame.equals(lastFrame)) {
                lastFrameRepeated++;
            }

            if (lastFrameRepeated < 3) {
                System.err.println("    at " + currentFrame);
            }

            lastFrame = currentFrame;
        }

        if (lastFrameRepeated > 0) {
            System.err.println("    [repeated " + lastFrameRepeated + " more time(-s)]");
        }

        halt();
    }

    public void halt() {
        halted = true;
    }

    public Stack<Object> getOperandStack() {
        return operandStack;
    }

    public Stack<Frame> getCallStack() {
        return callStack;
    }

    public static class Frame {
        private final AbstractFunction function;
        private final byte[] chunk;
        private final String[] constants;
        private final Map<Integer, Integer> lines;
        private int pc;

        public Frame(AbstractFunction function, byte[] chunk, String[] constants, Map<Integer, Integer> lines) {
            this.function = function;
            this.chunk = chunk;
            this.constants = constants;
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
            return chunk[pc++] << 24 | chunk[pc++] << 16 | chunk[pc++] << 8 | chunk[pc++];
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
            super(function, null, null, Collections.emptyMap());
        }

        @Override
        public String toString() {
            return getFunction().getModule().getName() + "/" + getFunction().getName() + "(Native Method)";
        }
    }
}
