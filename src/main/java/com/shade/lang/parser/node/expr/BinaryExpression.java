package com.shade.lang.parser.node.expr;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.gen.Opcode;
import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;
import com.shade.lang.parser.token.TokenKind;

public class BinaryExpression extends Expression {
    private final Expression lhs;
    private final Expression rhs;
    private final TokenKind operator;

    public BinaryExpression(Expression lhs, Expression rhs, TokenKind operator, Region region) {
        super(region);
        this.lhs = lhs;
        this.rhs = rhs;
        this.operator = operator;
    }

    @Override
    public void compile(Context context, Assembler assembler) {
        lhs.compile(context, assembler);
        rhs.compile(context, assembler);

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
            case Eq:
                assembler.imm8(Opcode.CMP_EQ);
                break;
            case NotEq:
                assembler.imm8(Opcode.CMP_NE);
                break;
            case Less:
                assembler.imm8(Opcode.CMP_LT);
                break;
            case LessEq:
                assembler.imm8(Opcode.CMP_LE);
                break;
            case Greater:
                assembler.imm8(Opcode.CMP_GT);
                break;
            case GreaterEq:
                assembler.imm8(Opcode.CMP_GE);
                break;
            default:
                throw new AssertionError("Unsupported binary operator: " + operator);
        }
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
}
