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
    private final Object[] constants;
    private final Map<Integer, Region.Span> lines;
    private final Assembler.Guard[] guards;
    private final int localsCount;

    public RuntimeFunction(Module module, String name, int flags, ByteBuffer chunk, Object[] constants, Map<Integer, Region.Span> lines, Assembler.Guard[] guards, int argumentsCount, int boundArgumentsCount, int localsCount) {
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

        machine.getCallStack().push(new Machine.Frame(this, chunk.array(), constants, slots));
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

    public int getLocalsCount() {
        return localsCount;
    }

    @Override
    public String toString() {
        if (getBoundArguments() != null) {
            return "[Function '" + getName() + "']";
        } else {
            return "[Bound Function '" + getName() + "']";
        }
    }
}
