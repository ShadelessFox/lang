package com.shade.lang.runtime.objects.module;

import com.shade.lang.runtime.objects.Chunk;
import com.shade.lang.runtime.objects.ScriptObject;
import com.shade.lang.util.annotations.NotNull;
import com.shade.lang.util.annotations.Nullable;

public class Module extends ScriptObject {
    private final String name;
    private final String source;
    private Chunk chunk;

    public Module(@NotNull String name, @NotNull String source) {
        super(true);
        this.name = name;
        this.source = source;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getSource() {
        return source;
    }

    @Nullable
    public Chunk getChunk() {
        return chunk;
    }

    public void setChunk(@Nullable Chunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public String toString() {
        return "[Module '" + name + "']";
    }
}
