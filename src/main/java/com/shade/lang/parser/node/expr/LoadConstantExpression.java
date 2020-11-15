package com.shade.lang.parser.node.expr;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.compiler.Opcode;
import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

import java.util.Objects;

public class LoadConstantExpression<T> extends Expression {
    private final T value;

    public LoadConstantExpression(T value, Region region) {
        super(region);
        this.value = value;
    }

    @Override
    public void compile(Context context, Assembler assembler) {
        if (value instanceof String || value instanceof Number || value instanceof Boolean || value == Void.TYPE) {
            assembler.imm8(Opcode.PUSH_CONST);
            assembler.imm32(assembler.addConstant(value));
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
