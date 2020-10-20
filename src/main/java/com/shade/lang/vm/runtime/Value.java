package com.shade.lang.vm.runtime;

public class Value extends ScriptObject {
    private final Object value;

    public Value(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
