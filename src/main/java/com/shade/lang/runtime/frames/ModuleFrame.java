package com.shade.lang.runtime.frames;

import com.shade.lang.runtime.objects.module.Module;
import com.shade.lang.util.annotations.NotNull;
import com.shade.lang.util.annotations.Nullable;
import com.shade.lang.tool.serialization.attributes.LineNumberTableAttribute;

public class ModuleFrame extends Frame {
    public ModuleFrame(@NotNull Module module, int stack) {
        super(module, module.getChunk(), null, stack);
    }

    @Override
    public String toString() {
        final LineNumberTableAttribute.Location location = getLocation(pc);
        if (location != null) {
            return getModule().getName() + " (" + getModule().getSource() + ':' + location + ')';
        } else {
            return getModule().getName() + " (" + getModule().getSource() + ":+" + pc + ')';
        }
    }

    @Nullable
    private LineNumberTableAttribute.Location getLocation(int pc) {
        final LineNumberTableAttribute attribute = getChunk().getSingleAttribute(LineNumberTableAttribute.DESCRIPTOR);
        if (attribute == null) {
            return null;
        }
        return attribute.getLocationByAddress(pc);
    }
}
