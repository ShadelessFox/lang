package com.shade.lang.vm.runtime;

import com.shade.lang.vm.Machine;

import java.util.Objects;

public class Value extends ScriptObject {
    private final Object value;

    public Value(Object value) {
        this.value = value;
    }

    public Value add(Machine machine, Value other) {
        if (value instanceof Integer && other.value instanceof Integer) {
            return new Value((int) value + (int) other.value);
        }

        return new Value(value.toString() + other.value.toString());
    }

    public Value sub(Machine machine, Value other) {
        if (value instanceof Integer && other.value instanceof Integer) {
            return new Value((int) value - (int) other.value);
        }

        machine.panic(String.format("Unsupported operands for operator '-': '%s' and '%s'", value, other.value));
        return null;
    }

    public Value mul(Machine machine, Value other) {
        if (value instanceof Integer && other.value instanceof Integer) {
            return new Value((int) value * (int) other.value);
        }

        machine.panic(String.format("Unsupported operands for operator '*': '%s' and '%s'", value, other.value));
        return null;
    }

    public Value div(Machine machine, Value other) {
        if (value instanceof Integer && other.value instanceof Integer) {
            return new Value((int) value / (int) other.value);
        }

        machine.panic(String.format("Unsupported operands for operator '/': '%s' and '%s'", value, other.value));
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue() {
        return (T) value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Value value1 = (Value) o;
        return Objects.equals(value, value1.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
