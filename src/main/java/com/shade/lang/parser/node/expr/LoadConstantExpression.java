package com.shade.lang.parser.node.expr;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.compiler.Operand;
import com.shade.lang.compiler.Operation;
import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;
import com.shade.lang.vm.runtime.value.NoneValue;

import java.util.Objects;

public class LoadConstantExpression<T> extends Expression {
    private final T value;

    public LoadConstantExpression(T value, Region region) {
        super(region);
        this.value = value;
    }

    @Override
    public void compile(Context context, Assembler assembler) {
        if (value instanceof String || value instanceof Number || value instanceof Boolean || value instanceof NoneValue) {
            assembler.emit(Operation.PUSH, Operand.constant(value));
            return;
        }

        throw new RuntimeException("Unsupported constant value: " + value + " (" + value.getClass() + ")");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoadConstantExpression<?> that = (LoadConstantExpression<?>) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public T getValue() {
        return value;
    }
}
