package com.shade.lang.runtime.frames;

import com.shade.lang.runtime.objects.Chunk;
import com.shade.lang.runtime.objects.ScriptObject;
import com.shade.lang.runtime.objects.module.Module;
import com.shade.lang.util.annotations.NotNull;

public class Frame {
    private final Module module;
    private final Chunk chunk;
    private final ScriptObject[] locals;
    private final int stack;
    public int pc;

    public Frame(@NotNull Module module, Chunk chunk, ScriptObject[] locals, int stack) {
        this.module = module;
        this.chunk = chunk;
        this.locals = locals;
        this.stack = stack;
    }

    public byte getNextImm8() {
        return chunk.getCode()[pc++];
    }

    public short getNextImm16() {
        short value = 0;
        value |= (chunk.getCode()[pc++] & 0xff) << 8;
        value |= (chunk.getCode()[pc++] & 0xff);
        return value;
    }

    public int getNextImm32() {
        int value = 0;
        value |= (chunk.getCode()[pc++] & 0xff) << 24;
        value |= (chunk.getCode()[pc++] & 0xff) << 16;
        value |= (chunk.getCode()[pc++] & 0xff) << 8;
        value |= (chunk.getCode()[pc++] & 0xff);
        return value;
    }

    @NotNull
    public Object getNextConstant() {
        return chunk.getConstants()[getNextImm16()];
    }

    @NotNull
    public Module getModule() {
        return module;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public ScriptObject[] getLocals() {
        return locals;
    }

    public int getStackSize() {
        return stack;
    }
}
