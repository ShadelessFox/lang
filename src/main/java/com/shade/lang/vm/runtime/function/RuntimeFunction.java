package com.shade.lang.vm.runtime.function;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.parser.token.Region;
import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.Module;
import com.shade.lang.vm.runtime.ScriptObject;
import com.shade.lang.vm.runtime.value.Value;

import java.nio.ByteBuffer;
import java.util.Map;

public class RuntimeFunction extends Function {
    private final ByteBuffer chunk;
    private final Object[] constants;
    private final Map<Integer, Region.Span> lines;
    private final Assembler.Guard[] guards;
    private final ScriptObject[] boundArguments;
    private final int boundArgumentsCount;
    private final int localsCount;

    public RuntimeFunction(Module module, String name, int flags, ByteBuffer chunk, Object[] constants, Map<Integer, Region.Span> lines, Assembler.Guard[] guards, int argumentsCount, int boundArgumentsCount, int localsCount) {
        super(module, name, argumentsCount, flags);
        this.chunk = chunk;
        this.constants = constants;
        this.lines = lines;
        this.guards = guards;
        this.boundArguments = boundArgumentsCount > 0 ? new ScriptObject[boundArgumentsCount] : null;
        this.boundArgumentsCount = boundArgumentsCount;
        this.localsCount = localsCount;
    }

    @Override
    public void invoke(Machine machine, int argc) {
        if (isInvalidArguments(machine, argc)) {
            return;
        }

        // TODO: Crazy (also partially shared) math here, refactor this please...

        boolean variadic = hasFlag(Function.FLAG_VARIADIC);

        ScriptObject[] arguments = new ScriptObject[localsCount];

        if (variadic) {
            ScriptObject[] variadicArguments = new ScriptObject[argc - argumentsCount + 1];
            arguments[argumentsCount + boundArgumentsCount - 1] = Value.from(variadicArguments);
            for (int index = 0; index < variadicArguments.length; index++) {
                variadicArguments[variadicArguments.length - index - 1] = machine.getOperandStack().pop();
            }
            for (int index = getArgumentsCount() - 1; index > 0; index--) {
                arguments[boundArgumentsCount + index - 1] = machine.getOperandStack().pop();
            }
        } else {
            for (int index = getArgumentsCount(); index > 0; index--) {
                arguments[boundArgumentsCount + index - 1] = machine.getOperandStack().pop();
            }
        }

        if (boundArgumentsCount > 0) {
            System.arraycopy(boundArguments, 0, arguments, 0, boundArgumentsCount);
        }

        Machine.Frame frame = new Machine.Frame(this, chunk.array(), constants, arguments);
        machine.getCallStack().push(frame);
    }

    public ByteBuffer getChunk() {
        return chunk;
    }

    public Map<Integer, Region.Span> getLines() {
        return lines;
    }

    public Assembler.Guard[] getGuards() {
        return guards;
    }

    public Object[] getConstants() {
        return constants;
    }

    public ScriptObject[] getBoundArguments() {
        return boundArguments;
    }

    public int getBoundArgumentsCount() {
        return boundArgumentsCount;
    }

    public int getLocalsCount() {
        return localsCount;
    }

    @Override
    public String toString() {
        if (boundArgumentsCount == 0) {
            return "[Function '" + getName() + "']";
        } else {
            return "[Bound Function '" + getName() + "']";
        }
    }
}
