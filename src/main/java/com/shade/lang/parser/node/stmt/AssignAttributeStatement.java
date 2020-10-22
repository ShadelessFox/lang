package com.shade.lang.parser.node.stmt;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.gen.Opcode;
import com.shade.lang.parser.node.Visitor;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.node.expr.Expression;
import com.shade.lang.parser.token.Region;

public class AssignAttributeStatement implements Statement {
    private final Expression target;
    private final String name;
    private final Expression value;
    private final Region region;

    public AssignAttributeStatement(Expression target, String name, Expression value, Region region) {
        this.target = target;
        this.name = name;
        this.value = value;
        this.region = region;
    }

    public String getName() {
        return name;
    }

    public Expression getValue() {
        return value;
    }

    public Expression getTarget() {
        return target;
    }

    @Override
    public boolean isControlFlowReturned() {
        return false;
    }

    @Override
    public Region getRegion() {
        return region;
    }

    @Override
    public void emit(Context context, Assembler assembler) {
        target.emit(context, assembler);
        value.emit(context, assembler);
        assembler.imm8(Opcode.SET_ATTRIBUTE);
        assembler.imm32(assembler.constant(name));
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
