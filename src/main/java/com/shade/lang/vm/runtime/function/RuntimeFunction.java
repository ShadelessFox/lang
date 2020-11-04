package com.shade.lang.vm.runtime.function;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.parser.token.Region;
import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.Module;
import com.shade.lang.vm.runtime.ScriptObject;

import java.nio.ByteBuffer;
import java.util.Map;

public class RuntimeFunction extends Function {
    private final ByteBuffer chunk;
    private final String[] constants;
    private final Map<Integer, Region.Span> lines;
    private final Assembler.Guard[] guards;
    private final ScriptObject[] boundArguments;
    private final int boundArgumentsCount;
    private final int argumentsCount;
    private final int localsCount;

    public RuntimeFunction(Module module, String name, ByteBuffer chunk, String[] constants, Map<Integer, Region.Span> lines, Assembler.Guard[] guards, int argumentsCount, int boundArgumentsCount, int localsCount) {
        super(module, name);
        this.chunk = chunk;
        this.constants = constants;
        this.lines = lines;
        this.guards = guards;
        this.boundArguments = boundArgumentsCount > 0 ? new ScriptObject[boundArgumentsCount] : null;
        this.argumentsCount = argumentsCount;
        this.boundArgumentsCount = boundArgumentsCount;
        this.localsCount = localsCount;
    }

    @Override
    public void invoke(Machine machine, int argc) {
        if (argumentsCount != argc) {
            machine.panic("Function '" + getName() + "' accepts " + argumentsCount + " argument(-s) but " + argc + " provided", true);
            return;
        }

        ScriptObject[] objects = new ScriptObject[localsCount];

        if (boundArgumentsCount > 0) {
            System.arraycopy(boundArguments, 0, objects, 0, boundArgumentsCount);
        }

        for (int index = argumentsCount; index > 0; index--) {
            objects[boundArgumentsCount + index - 1] = machine.getOperandStack().pop();
        }

        Machine.Frame frame = new Machine.Frame(this, chunk.array(), constants, objects);
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

    public String[] getConstants() {
        return constants;
    }

    public ScriptObject[] getBoundArguments() {
        return boundArguments;
    }

    public int getBoundArgumentsCount() {
        return boundArgumentsCount;
    }

    public int getArgumentsCount() {
        return argumentsCount;
    }

    public int getLocalsCount() {
        return localsCount;
    }
}
