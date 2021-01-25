package com.shade.lang.runtime.frames;

import com.shade.lang.runtime.objects.function.NativeFunction;
import com.shade.lang.runtime.objects.module.Module;
import com.shade.lang.util.annotations.NotNull;

import java.util.Objects;

public class NativeFrame extends Frame {
    private final NativeFunction function;

    public NativeFrame(@NotNull Module module, @NotNull NativeFunction function, int stack) {
        super(module, null, null, stack);
        this.function = function;
    }

    @NotNull
    public NativeFunction getFunction() {
        return function;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NativeFrame that = (NativeFrame) o;
        return function.equals(that.function);
    }

    @Override
    public int hashCode() {
        return Objects.hash(function);
    }

    @Override
    public String toString() {
        return getModule().getSource() + '/' + function.getName() + " (Native Function)";
    }
}
