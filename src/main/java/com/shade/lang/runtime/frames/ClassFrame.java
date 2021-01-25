package com.shade.lang.runtime.frames;

import com.shade.lang.runtime.objects.Chunk;
import com.shade.lang.runtime.objects.Class;
import com.shade.lang.runtime.objects.ScriptObject;
import com.shade.lang.runtime.objects.module.Module;
import com.shade.lang.util.annotations.NotNull;

public class ClassFrame extends Frame {
    public ClassFrame(@NotNull Module module, @NotNull Class clazz, @NotNull Chunk chunk, int stack) {
        super(module, chunk, new ScriptObject[]{clazz}, stack);
    }
}
