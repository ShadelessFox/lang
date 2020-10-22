package com.shade.lang.parser.node.expr;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.gen.Opcode;
import com.shade.lang.parser.node.Visitor;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;
import com.shade.lang.parser.token.TokenKind;

public class UnaryExpression implements Expression {
    private final Expression rhs;
    private final TokenKind operator;
    private final Region region;

    public UnaryExpression(Expression rhs, TokenKind operator, Region region) {
        this.rhs = rhs;
        this.operator = operator;
        this.region = region;
    }

    public Expression getRhs() {
        return rhs;
    }

    public TokenKind getOperator() {
        return operator;
    }

    @Override
    public Region getRegion() {
        return region;
    }

    @Override
    public void emit(Context context, Assembler assembler) {
        rhs.emit(context, assembler);

        switch (operator) {
            case Add:
                // TODO: Maybe add an implicit conversion to positive number?
                break;
            case Sub:
                assembler.imm8(Opcode.PUSH_INT);
                assembler.imm32(-1);
                assembler.imm8(Opcode.MUL);
                break;
            case Not:
                assembler.imm8(Opcode.NOT);
                break;
        }
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
