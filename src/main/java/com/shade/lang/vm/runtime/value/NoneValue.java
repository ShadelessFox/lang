package com.shade.lang.vm.runtime.value;

import com.shade.lang.vm.Machine;

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
