package com.shade.lang.compiler.parser.node.expr;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.assembler.Operand;
import com.shade.lang.compiler.assembler.Operation;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Expression;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.compiler.parser.token.TokenKind;
import com.shade.lang.runtime.objects.value.NoneValue;

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
        if (operator != TokenKind.Sub) {
            // Negation requires special treatment
            rhs.compile(context, assembler);
        }

        switch (operator) {
            case Add:
                break;
            case Sub:
                assembler.emit(Operation.PUSH, Operand.constant(0));
                rhs.compile(context, assembler);
                assembler.emit(Operation.SUB);
                break;
            case Not:
                assembler.emit(Operation.NOT);
                break;
            case Try:
                assembler.emit(Operation.DUP);
                assembler.emit(Operation.PUSH, Operand.constant(NoneValue.INSTANCE));
                assembler.emit(Operation.CMP_EQ);
                Assembler.Label end = assembler.jump(Operation.JUMP_IF_FALSE);
                assembler.emit(Operation.RETURN);
                assembler.bind(end);
                break;
            default:
                throw new AssertionError("Unsupported unary operator: " + operator);
        }

        assembler.addLocation(getRegion().getBegin());
    }

    public Expression getRhs() {
        return rhs;
    }

    public TokenKind getOperator() {
        return operator;
    }
}
