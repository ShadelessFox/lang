package com.shade.lang.runtime.objects;

import com.shade.lang.runtime.objects.function.Function;
import com.shade.lang.util.annotations.NotNull;

public class Class extends ScriptObject {
    private final String name;
    private final Class[] bases;

    public Class(String name, Class[] bases) {
        super(false);
        this.name = name;
        this.bases = bases;
    }

    public Instance instantiate() {
        throw new AssertionError("Not implemented");
    }

    public boolean isDerivedFrom(@NotNull Class cls) {
        if (this == cls) {
            return true;
        }

        for (Class base : bases) {
            if (base.isDerivedFrom(cls)) {
                return true;
            }
        }

        return false;
    }

    public boolean isInstance(@NotNull Instance instance) {
        return instance.getBase().isDerivedFrom(this);
    }

    private String getFunctionName(Function function) {
        return name + "::" + function.getName();
    }

    public String getName() {
        return name;
    }

    public Class[] getBases() {
        return bases;
    }

    @Override
    public String toString() {
        return "[Class " + name + "]";
    }
}
