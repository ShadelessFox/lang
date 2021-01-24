package com.shade.lang.runtime.objects.function;

import com.shade.lang.runtime.Machine;
import com.shade.lang.runtime.objects.ScriptObject;
import com.shade.lang.runtime.objects.module.Module;
import com.shade.lang.util.Pair;

import java.util.Map;

public class RuntimeFunction extends Function {
    private final byte[] chunk;
    private final Object[] constants;
    private final Map<Integer, Pair<Short, Short>> lines;
    private final Guard[] guards;
    private final int localsCount;

    public RuntimeFunction(Module module, String name, int flags, byte[] chunk, Object[] constants, Map<Integer, Pair<Short, Short>> lines, Guard[] guards, int argumentsCount, int boundArgumentsCount, int localsCount) {
        super(module, name, argumentsCount, new ScriptObject[boundArgumentsCount], flags);
        this.chunk = chunk;
        this.constants = constants;
        this.lines = lines;
        this.guards = guards;
        this.localsCount = localsCount;
    }

    @Override
    public void invoke(Machine machine, int argc) {
        ScriptObject[] arguments = prepare(machine, argc);

        if (arguments == null) {
            return;
        }

        ScriptObject[] slots = new ScriptObject[localsCount];
        System.arraycopy(arguments, 0, slots, 0, arguments.length);

        Machine.Frame frame = new Machine.Frame(module, this, chunk, constants, slots, machine.getOperandStack().size());
        machine.profilerBeginFrame(frame);
        machine.getCallStack().push(frame);
    }

    public byte[] getChunk() {
        return chunk;
    }

    public Map<Integer, Pair<Short, Short>> getLocations() {
        return lines;
    }

    public Guard[] getGuards() {
        return guards;
    }

    public Object[] getConstants() {
        return constants;
    }

    public int getLocalsCount() {
        return localsCount;
    }

    @Override
    public String toString() {
        if (getBoundArguments().length == 0) {
            return "[Function '" + getName() + "']";
        } else {
            return "[Bound Function '" + getName() + "']";
        }
    }
}
