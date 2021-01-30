package com.shade.lang.runtime.frames;

import com.shade.lang.runtime.objects.module.Module;
import com.shade.lang.util.Pair;
import com.shade.lang.util.annotations.NotNull;

public class ModuleFrame extends Frame {
    public ModuleFrame(@NotNull Module module, int stack) {
        super(module, module.getChunk(), null, stack);
    }

    @Override
    public String toString() {
        final Pair<Short, Short> line = getChunk().getLocations().get(pc);

        if (line != null) {
            return getModule().getName() + " (" + getModule().getSource() + ':' + line.getFirst() + ':' + line.getSecond() + ')';
        } else {
            return getModule().getName() + " (" + getModule().getSource() + ":+" + pc + ')';
        }
    }
}
