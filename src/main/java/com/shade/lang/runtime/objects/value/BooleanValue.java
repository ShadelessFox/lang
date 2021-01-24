package com.shade.lang.runtime.objects.value;

import com.shade.lang.runtime.Machine;

public class BooleanValue extends Value {
    private final boolean value;

    public BooleanValue(boolean value) {
        this.value = value;
    }

    @Override
    public Boolean getBoolean(Machine machine) {
        return value;
    }

    @Override
    public Object getValue() {
        return value;
    }
}
