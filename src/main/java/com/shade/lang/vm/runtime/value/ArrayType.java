package com.shade.lang.vm.runtime.value;

import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.ScriptObject;
import com.shade.lang.vm.runtime.extension.Index;
import com.shade.lang.vm.runtime.extension.MutableIndex;

public class ArrayType extends Value implements Index, MutableIndex {
    private final ScriptObject[] values;

    public ArrayType(ScriptObject[] values) {
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
}
