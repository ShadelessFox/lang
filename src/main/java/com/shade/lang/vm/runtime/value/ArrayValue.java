package com.shade.lang.vm.runtime.value;

import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.ScriptObject;
import com.shade.lang.vm.runtime.extension.Index;
import com.shade.lang.vm.runtime.extension.MutableIndex;

import java.util.Arrays;

public class ArrayValue extends Value implements Index, MutableIndex {
    private final ScriptObject[] values;

    public ArrayValue(ScriptObject[] values) {
        this.values = values;
        setAttribute("length", Value.from(values.length));
    }

    @Override
    public ScriptObject getIndex(Machine machine, ScriptObject index) {
        Integer idx = getIndexValue(machine, index);
        if (idx != null) {
            return values[idx];
        }
        return null;
    }

    @Override
    public void setIndex(Machine machine, ScriptObject index, ScriptObject value) {
        Integer idx = getIndexValue(machine, index);
        if (idx != null) {
            values[idx] = value;
        }
    }

    private Integer getIndexValue(Machine machine, ScriptObject index) {
        if (!(index instanceof IntegerValue)) {
            machine.panic("Expected index to be integer");
            return null;
        }

        int idx = ((IntegerValue) index).getValue();

        if (idx >= values.length) {
            machine.panic("Index out of range (index is " + idx + ", size is " + values.length + ")");
            return null;
        }

        return idx;
    }

    @Override
    public Object getValue() {
        return values;
    }

    public ScriptObject[] getValues() {
        return values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrayValue value1 = (ArrayValue) o;
        return Arrays.equals(values, value1.values);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        for (int index = 0; ; index++) {
            builder.append(values[index].toDisplayString());
            if (index == values.length - 1)
                return builder.append(']').toString();
            builder.append(", ");
        }
    }
}
