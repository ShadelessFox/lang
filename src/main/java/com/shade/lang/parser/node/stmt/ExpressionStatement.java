package com.shade.lang.parser.node.stmt;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.gen.Opcode;
import com.shade.lang.parser.node.Visitor;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.node.expr.Expression;
import com.shade.lang.parser.token.Region;

public class ExpressionStatement implements Statement {
    private final Expression expression;
    private final Region region;

    public ExpressionStatement(Expression expression, Region region) {
        this.expression = expression;
        this.region = region;
    }

    public Expression getExpression() {
        return expression;
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
        expression.emit(context, assembler);
        assembler.imm8(Opcode.POP);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
