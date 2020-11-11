package com.shade.lang.vm.runtime.value;

import com.shade.lang.vm.Machine;

public class NumberValue extends Value {
    private final int value;

    public NumberValue(int value) {
        this.value = value;
    }

    @Override
    public Value add(Machine machine, Value other) {
        if (other instanceof NumberValue) {
            return new NumberValue(value + ((NumberValue) other).value);
        }
        return super.add(machine, other);
    }

    @Override
    public Value sub(Machine machine, Value other) {
        if (other instanceof NumberValue) {
            return new NumberValue(value - ((NumberValue) other).value);
        }
        return super.sub(machine, other);
    }

    @Override
    public Value mul(Machine machine, Value other) {
        if (other instanceof NumberValue) {
            return new NumberValue(value * ((NumberValue) other).value);
        }
        return super.mul(machine, other);
    }

    @Override
    public Value div(Machine machine, Value other) {
        if (other instanceof NumberValue) {
            return new NumberValue(value / ((NumberValue) other).value);
        }
        return super.div(machine, other);
    }

    @Override
    public Integer compare(Machine machine, Value other) {
        if (other instanceof NumberValue) {
            return Integer.compare(value, ((NumberValue) other).value);
        }
        return super.compare(machine, other);
    }

    @Override
    public Boolean getBoolean(Machine machine) {
        return value > 0;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
