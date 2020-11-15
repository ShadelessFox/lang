package com.shade.lang.vm.runtime.value;

import com.shade.lang.vm.Machine;

public class FloatValue extends Value {
    private final float value;

    public FloatValue(float value) {
        this.value = value;
    }

    @Override
    public Value add(Machine machine, Value other) {
        if (other instanceof FloatValue) {
            return new FloatValue(value + ((FloatValue) other).value);
        }
        if (other instanceof IntegerValue) {
            return new FloatValue(value + ((IntegerValue) other).getValue());
        }
        return super.add(machine, other);
    }

    @Override
    public Value sub(Machine machine, Value other) {
        if (other instanceof FloatValue) {
            return new FloatValue(value - ((FloatValue) other).value);
        }
        if (other instanceof IntegerValue) {
            return new FloatValue(value - ((IntegerValue) other).getValue());
        }
        return super.sub(machine, other);
    }

    @Override
    public Value mul(Machine machine, Value other) {
        if (other instanceof FloatValue) {
            return new FloatValue(value * ((FloatValue) other).value);
        }
        if (other instanceof IntegerValue) {
            return new FloatValue(value * ((IntegerValue) other).getValue());
        }
        return super.mul(machine, other);
    }

    @Override
    public Value div(Machine machine, Value other) {
        if (other instanceof FloatValue) {
            return new FloatValue(value / ((FloatValue) other).value);
        }
        if (other instanceof IntegerValue) {
            return new FloatValue(value / ((IntegerValue) other).getValue());
        }
        return super.div(machine, other);
    }

    @Override
    public Integer compare(Machine machine, Value other) {
        if (other instanceof FloatValue) {
            return Float.compare(value, ((FloatValue) other).value);
        }
        if (other instanceof IntegerValue) {
            return Float.compare(value, ((IntegerValue) other).getValue());
        }
        return super.compare(machine, other);
    }

    @Override
    public Float getValue() {
        return value;
    }
}
