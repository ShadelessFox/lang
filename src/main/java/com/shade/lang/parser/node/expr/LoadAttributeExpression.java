package com.shade.lang.parser.node.expr;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.gen.Opcode;
import com.shade.lang.parser.node.Visitor;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

public class LoadAttributeExpression implements Expression {
    private final Expression owner;
    private final String name;
    private final Region region;

    public LoadAttributeExpression(Expression owner, String name, Region region) {
        this.owner = owner;
        this.name = name;
        this.region = region;
    }

    public Expression getOwner() {
        return owner;
    }

    public String getName() {
        return name;
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
        owner.emit(context, assembler);
        assembler.imm8(Opcode.GET_ATTRIBUTE);
        assembler.imm32(assembler.constant(name));
    }
}
