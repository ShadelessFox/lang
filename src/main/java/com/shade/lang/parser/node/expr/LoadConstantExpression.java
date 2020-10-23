package com.shade.lang.parser.node.expr;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.gen.Opcode;
import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

public class LoadConstantExpression<T> extends Expression {
    private final T value;

    public LoadConstantExpression(T value, Region region) {
        super(region);
        this.value = value;
    }

    @Override
    public void compile(Context context, Assembler assembler) {
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

    public T getValue() {
        return value;
    }
}
