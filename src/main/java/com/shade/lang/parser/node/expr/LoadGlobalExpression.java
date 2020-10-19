package com.shade.lang.parser.node.expr;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.gen.Opcode;
import com.shade.lang.parser.node.Visitor;
import com.shade.lang.vm.runtime.Module;

public class LoadGlobalExpression implements Expression {
    private final String name;

    public LoadGlobalExpression(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void emit(Module module, Assembler assembler) {
        assembler.imm8(Opcode.GET_GLOBAL);
        assembler.imm32(assembler.constant(name));
    }
}
