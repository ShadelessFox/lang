package com.shade.lang.parser.node.expr;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.gen.Opcode;
import com.shade.lang.parser.node.Visitor;
import com.shade.lang.vm.runtime.Module;

public class LoadAttributeExpression implements Expression {
    private final Expression owner;
    private final String name;

    public LoadAttributeExpression(Expression owner, String name) {
        this.owner = owner;
        this.name = name;
    }

    public Expression getOwner() {
        return owner;
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
        owner.emit(module, assembler);
        assembler.imm8(Opcode.GET_ATTRIBUTE);
        assembler.imm32(assembler.constant(name));
    }
}
