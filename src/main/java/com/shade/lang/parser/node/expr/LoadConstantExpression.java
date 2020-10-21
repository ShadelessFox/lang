package com.shade.lang.parser.node.expr;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.gen.Opcode;
import com.shade.lang.parser.node.Visitor;
import com.shade.lang.vm.runtime.Module;

public class LoadConstantExpression<T> implements Expression {
    private final T value;

    public LoadConstantExpression(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void emit(Module module, Assembler assembler) {
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
