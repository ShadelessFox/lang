package com.shade.lang.runtime.objects.value;

import com.shade.lang.runtime.Machine;

public class NoneValue extends Value {
    public static final NoneValue INSTANCE = new NoneValue();

    private NoneValue() {
    }

    @Override
    public Boolean getBoolean(Machine machine) {
        return false;
    }

    @Override
    public Object getValue() {
        return "none";
    }
}
