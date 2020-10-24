package com.shade.lang.parser.node.expr;

import com.shade.lang.parser.ScriptException;
import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.gen.Opcode;
import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;
import com.shade.lang.parser.token.TokenKind;

public class UnaryExpression extends Expression {
    private final Expression rhs;
    private final TokenKind operator;

    public UnaryExpression(Expression rhs, TokenKind operator, Region region) {
        super(region);
        this.rhs = rhs;
        this.operator = operator;
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        rhs.compile(context, assembler);

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
            default:
                throw new AssertionError("Unsupported unary operator: " + operator);
        }
    }

    public Expression getRhs() {
        return rhs;
    }

    public TokenKind getOperator() {
        return operator;
    }
}
