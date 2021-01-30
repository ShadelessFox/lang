package com.shade.lang.runtime.objects.value;

import com.shade.lang.runtime.Machine;
import com.shade.lang.runtime.objects.ScriptObject;
import com.shade.lang.runtime.objects.extension.Index;

public class StringValue extends Value implements Index {
    private final String value;

    public StringValue(String value) {
        this.value = value;
        setAttribute("length", Value.from(value.length()));
    }

    @Override
    public Value add(Machine machine, Value other) {
        return new StringValue(value + other);
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
    public ScriptObject getIndex(Machine machine, ScriptObject index) {
        if (!(index instanceof IntegerValue)) {
            machine.panic("Expected index to be integer", true);
            return null;
        }

        int idx = ((IntegerValue) index).getValue();

        if (idx >= value.length()) {
            machine.panic("Index out of range (index is " + idx + ", size is " + value.length() + ")", true);
            return null;
        }

        return Value.from(String.valueOf(value.charAt(idx)));
    }

    @Override
    public String toDisplayString() {
        return '\'' + value + '\'';
    }
}
