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
        super(module, name, chunk.getArguments(), chunk.getFlags());
        this.chunk = chunk;
    }

    @Override
    protected void invoke(@NotNull Machine machine, @NotNull ScriptObject[] arguments) {
        final ScriptObject[] locals = new ScriptObject[chunk.getLocals()];
        System.arraycopy(arguments, 0, locals, 0, arguments.length);

        final Frame frame = new RuntimeFrame(module, this, locals, machine.getOperandStack().size());

        machine.profilerBeginFrame(frame);
        machine.getCallStack().push(frame);
    }

    @NotNull
    public Chunk getChunk() {
        return chunk;
    }

    @Override
    public String toString() {
        return "[Function '" + name + "']";
    }
}
