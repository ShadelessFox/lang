package com.shade.lang.runtime.frames;

import com.shade.lang.runtime.objects.ScriptObject;
import com.shade.lang.runtime.objects.function.RuntimeFunction;
import com.shade.lang.runtime.objects.module.Module;
import com.shade.lang.util.Pair;
import com.shade.lang.util.annotations.NotNull;
import com.shade.lang.util.annotations.Nullable;

import java.util.Objects;

public class RuntimeFrame extends Frame {
    private final RuntimeFunction function;

    public RuntimeFrame(@NotNull Module module, @NotNull RuntimeFunction function, @Nullable ScriptObject[] locals, int stack) {
        super(module, function.getChunk(), locals, stack);
        this.function = function;
    }

    @NotNull
    public RuntimeFunction getFunction() {
        return function;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RuntimeFrame that = (RuntimeFrame) o;
        return function.equals(that.function);
    }

    @Override
    public int hashCode() {
        return Objects.hash(function);
    }

    @Override
    public String toString() {
        final Pair<Short, Short> line = getChunk().getLocations().get(pc);

        if (line != null) {
            return getModule().getName() + '/' + function.getName() + " (" + getModule().getSource() + ':' + line.getFirst() + ':' + line.getSecond() + ')';
        } else {
            return getModule().getName() + '/' + function.getName() + " (" + getModule().getSource() + ":+" + pc + ')';
        }
    }
}
