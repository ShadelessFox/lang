package com.shade.lang.runtime.objects.function;

import com.shade.lang.runtime.Machine;
import com.shade.lang.runtime.frames.Frame;
import com.shade.lang.runtime.frames.RuntimeFrame;
import com.shade.lang.runtime.objects.Chunk;
import com.shade.lang.runtime.objects.ScriptObject;
import com.shade.lang.runtime.objects.module.Module;
import com.shade.lang.util.annotations.NotNull;

public class RuntimeFunction extends Function {
    private final Chunk chunk;

    public RuntimeFunction(@NotNull Module module, @NotNull String name, @NotNull Chunk chunk) {
        super(module, name, chunk.getArguments(), new ScriptObject[chunk.getBoundArguments()], chunk.getFlags());
        this.chunk = chunk;
    }

    @Override
    public void invoke(Machine machine, int argc) {
        ScriptObject[] arguments = prepare(machine, argc);

        if (arguments == null) {
            return;
        }

        ScriptObject[] slots = new ScriptObject[chunk.getLocals()];
        System.arraycopy(arguments, 0, slots, 0, arguments.length);

        Frame frame = new RuntimeFrame(module, this, slots, machine.getOperandStack().size());
        machine.profilerBeginFrame(frame);
        machine.getCallStack().push(frame);
    }

    @NotNull
    public Chunk getChunk() {
        return chunk;
    }

    @Override
    public String toString() {
        if (chunk.getBoundArguments() == 0) {
            return "[Function '" + getName() + "']";
        } else {
            return "[Bound Function '" + getName() + "']";
        }
    }
}
