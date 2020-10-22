package com.shade.lang.parser.node.stmt;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.gen.Opcode;
import com.shade.lang.parser.node.Visitor;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.node.expr.Expression;
import com.shade.lang.parser.token.Region;

public class ReturnStatement implements Statement {
    private final Expression value;
    private final Region region;

    public ReturnStatement(Expression value, Region region) {
        this.value = value;
        this.region = region;
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public boolean isControlFlowReturned() {
        return true;
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
        value.emit(context, assembler);
        assembler.span(region.getBegin());
        assembler.imm8(Opcode.RET);
    }
}
