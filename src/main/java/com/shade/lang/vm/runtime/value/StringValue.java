package com.shade.lang.vm.runtime.value;

import com.shade.lang.vm.Machine;

public class StringValue extends Value {
    private final String value;

    public StringValue(String value) {
        this.value = value;
    }

    @Override
    public Value add(Machine machine, Value other) {
        return new StringValue(value + other.getValue());
    }

    @Override
    public Boolean getBoolean(Machine machine) {
        return value.length() > 0;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public String toDisplayString() {
        return '\'' + value + '\'';
    }
}
