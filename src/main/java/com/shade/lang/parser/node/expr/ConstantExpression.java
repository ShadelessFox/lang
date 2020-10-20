package com.shade.lang.parser.node.expr;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.gen.Opcode;
import com.shade.lang.parser.node.Visitor;
import com.shade.lang.vm.runtime.Module;

public class ConstantExpression implements Expression {
    private final String value;

    public ConstantExpression(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void emit(Module module, Assembler assembler) {
        assembler.imm8(Opcode.PUSH_CONST);
        assembler.imm32(assembler.constant(value));
    }
}
