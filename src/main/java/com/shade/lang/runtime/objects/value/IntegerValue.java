package com.shade.lang.runtime.objects.value;

import com.shade.lang.runtime.Machine;

public class IntegerValue extends Value {
    private final int value;

    public IntegerValue(int value) {
        this.value = value;
    }

    @Override
    public Value add(Machine machine, Value other) {
        if (other instanceof IntegerValue) {
            return new IntegerValue(value + ((IntegerValue) other).value);
        }
        if (other instanceof FloatValue) {
            return new FloatValue(value + ((FloatValue) other).getValue());
        }
        return super.add(machine, other);
    }

    @Override
    public Value sub(Machine machine, Value other) {
        if (other instanceof IntegerValue) {
            return new IntegerValue(value - ((IntegerValue) other).value);
        }
        if (other instanceof FloatValue) {
            return new FloatValue(value - ((FloatValue) other).getValue());
        }
        return super.sub(machine, other);
    }

    @Override
    public Value mul(Machine machine, Value other) {
        if (other instanceof IntegerValue) {
            return new IntegerValue(value * ((IntegerValue) other).value);
        }
        if (other instanceof FloatValue) {
            return new FloatValue(value * ((FloatValue) other).getValue());
        }
        return super.mul(machine, other);
    }

    @Override
    public Value div(Machine machine, Value other) {
        if (other instanceof IntegerValue) {
            return new IntegerValue(value / ((IntegerValue) other).value);
        }
        if (other instanceof FloatValue) {
            return new FloatValue(value / ((FloatValue) other).getValue());
        }
        return super.div(machine, other);
    }

    @Override
    public Integer compare(Machine machine, Value other) {
        if (other instanceof IntegerValue) {
            return Integer.compare(value, ((IntegerValue) other).value);
        }
        if (other instanceof FloatValue) {
            return Float.compare(value, ((FloatValue) other).getValue());
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
