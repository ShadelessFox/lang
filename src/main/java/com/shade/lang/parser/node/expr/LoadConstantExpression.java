package com.shade.lang.parser.node.expr;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.gen.Opcode;
import com.shade.lang.parser.node.Visitor;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

public class LoadConstantExpression<T> implements Expression {
    private final T value;
    private final Region region;

    public LoadConstantExpression(T value, Region region) {
        this.value = value;
        this.region = region;
    }

    public T getValue() {
        return value;
    }

    @Override
    public Region getRegion() {
        return region;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void emit(Context context, Assembler assembler) {
        if (value instanceof String) {
            assembler.imm8(Opcode.PUSH_CONST);
            assembler.imm32(assembler.constant((String) value));
            return;
        }

        if (value instanceof Integer) {
            assembler.imm8(Opcode.PUSH_INT);
            assembler.imm32((Integer) value);
            return;
        }

        throw new RuntimeException("Unsupported constant value: " + value + " (" + value.getClass() + ")");
    }
}
