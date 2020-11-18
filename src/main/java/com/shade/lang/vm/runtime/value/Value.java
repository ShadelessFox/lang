package com.shade.lang.vm.runtime.value;

import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.ScriptObject;

import java.util.Objects;

public abstract class Value extends ScriptObject {
    protected Value() {
        super(true);
    }

    public static Value from(Object value) {
        if (value == null) {
            return null;
        } else if (value == Void.TYPE) {
            return NoneValue.INSTANCE;
        } else if (value instanceof Integer) {
            return new IntegerValue((int) value);
        } else if (value instanceof Float) {
            return new FloatValue((float) value);
        } else if (value instanceof String) {
            return new StringValue((String) value);
        } else if (value instanceof Boolean) {
            return new BooleanValue((Boolean) value);
        } else if (value instanceof ScriptObject[]) {
            return new ArrayValue((ScriptObject[]) value);
        }
        throw new IllegalArgumentException("Unsupported value: " + value.getClass());
    }

    public abstract Object getValue();

    public Value add(Machine machine, Value other) {
        machine.panic(String.format("Unsupported operands for operator '+': '%s' and '%s'", getValue(), other.getValue()), true);
        return null;
    }

    public Value sub(Machine machine, Value other) {
        machine.panic(String.format("Unsupported operands for operator '-': '%s' and '%s'", getValue(), other.getValue()), true);
        return null;
    }

    public Value mul(Machine machine, Value other) {
        machine.panic(String.format("Unsupported operands for operator '*': '%s' and '%s'", getValue(), other.getValue()), true);
        return null;
    }

    public Value div(Machine machine, Value other) {
        machine.panic(String.format("Unsupported operands for operator '/': '%s' and '%s'", getValue(), other.getValue()), true);
        return null;
    }

    public Integer compare(Machine machine, Value other) {
        machine.panic(String.format("Cannot compare values: '%s' and '%s'", getValue(), other.getValue()), true);
        return null;
    }

    public Boolean getBoolean(Machine machine) {
        machine.panic(String.format("Cannot reinterpret value as a boolean: '%s'", getValue()), true);
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Value value1 = (Value) o;
        return Objects.equals(getValue(), value1.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue());
    }

    @Override
    public String toString() {
        return getValue().toString();
    }
}
