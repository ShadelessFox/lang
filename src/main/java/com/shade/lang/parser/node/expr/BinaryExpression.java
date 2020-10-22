package com.shade.lang.parser.node.expr;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.gen.Opcode;
import com.shade.lang.parser.node.Visitor;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;
import com.shade.lang.parser.token.TokenKind;

public class BinaryExpression implements Expression {
    private final Expression lhs;
    private final Expression rhs;
    private final TokenKind operator;
    private final Region region;

    public BinaryExpression(Expression lhs, Expression rhs, TokenKind operator, Region region) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.operator = operator;
        this.region = region;
    }

    public Expression getLhs() {
        return lhs;
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
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void emit(Context context, Assembler assembler) {
        lhs.emit(context, assembler);
        rhs.emit(context, assembler);

        switch (operator) {
            case Add:
                assembler.imm8(Opcode.ADD);
                break;
            case Sub:
                assembler.imm8(Opcode.SUB);
                break;
            case Mul:
                assembler.imm8(Opcode.MUL);
                break;
            case Div:
                assembler.imm8(Opcode.DIV);
                break;
        }
    }
}
