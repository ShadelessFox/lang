package com.shade.lang.parser.node.expr;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.compiler.Opcode;
import com.shade.lang.parser.ScriptException;
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
                break;
            case Sub:
                assembler.imm8(Opcode.PUSH_CONST);
                assembler.imm32(assembler.addConstant(-1));
                assembler.imm8(Opcode.MUL);
                break;
            case Not:
                assembler.imm8(Opcode.NOT);
                break;
            case Try:
                assembler.imm8(Opcode.DUP);
                assembler.imm8(Opcode.PUSH_CONST);
                assembler.imm32(assembler.addConstant(Void.TYPE));
                assembler.imm8(Opcode.CMP_EQ);
                Assembler.Label end = assembler.jump(Opcode.JUMP_IF_FALSE);
                assembler.imm8(Opcode.RET);
                assembler.bind(end);
                break;
            default:
                throw new AssertionError("Unsupported unary operator: " + operator);
        }

        assembler.addTraceLine(getRegion().getBegin());
    }

    public Expression getRhs() {
        return rhs;
    }

    public TokenKind getOperator() {
        return operator;
    }
}
