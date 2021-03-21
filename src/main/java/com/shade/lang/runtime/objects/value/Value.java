package com.shade.lang.runtime.objects.value;

import com.shade.lang.runtime.Machine;
import com.shade.lang.runtime.objects.ScriptObject;
import com.shade.lang.util.annotations.NotNull;

import java.util.Objects;
import java.util.Stack;

public abstract class Value extends ScriptObject {
    protected Value() {
        super(true);
    }

    protected Value(boolean immutable) {
        super(immutable);
    }

    public static Value from(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof Integer) {
            return new IntegerValue((int) value);
        } else if (value instanceof Long) {
            return new IntegerValue((int) (long) value);
        } else if (value instanceof Float) {
            return new FloatValue((float) value);
        } else if (value instanceof String) {
            return new StringValue((String) value);
        } else if (value instanceof Boolean) {
            return new BooleanValue((Boolean) value);
        } else if (value instanceof ScriptObject[]) {
            return new ArrayValue((ScriptObject[]) value);
        } else if (value instanceof Value) {
            return (Value) value;
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

    /**
     * The print stack used to track cyclic objects.
     */
    private static final Stack<ScriptObject> PRINT_STACK = new Stack<>();

    /**
     * Pushes the supplied <code>object</code> onto the print stack (if not present) to prevent
     * cyclic {@link #toString()} evaluation of the same object by tracking values currently being printed.
     * <p>
     *
     * @param object the object to push onto the print stack
     * @return <code>true</code> if the supplied <code>object</code>was already tracked, <code>false</code> otherwise.
     * If so, {@link #leavePrint(ScriptObject)} must be called with the same object.
     */
    protected static boolean enterPrint(@NotNull ScriptObject object) {
        for (int index = PRINT_STACK.size() - 1; index >= 0; index--) {
            if (PRINT_STACK.get(index) == object) {
                return true;
            }
        }
        PRINT_STACK.push(object);
        return false;
    }

    /**
     * Removes the supplied <code>object</code> from the print stack. This function must be called only if
     * invocation of {@link #enterPrint(ScriptObject)} with the same object resulted in <code>false</code>.
     *
     * @param object the object to remove from the print stack
     */
    protected static void leavePrint(@NotNull ScriptObject object) {
        if (PRINT_STACK.peek() != object) {
            throw new IllegalArgumentException("leavePrint must be preceded by enterPrint on the same object");
        }
        PRINT_STACK.pop();
    }
}
