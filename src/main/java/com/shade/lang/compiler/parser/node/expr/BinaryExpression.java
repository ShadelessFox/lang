package com.shade.lang.compiler.parser.node.expr;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.assembler.Operation;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Expression;
import com.shade.lang.compiler.parser.node.visitor.Visitor;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.compiler.parser.token.TokenKind;
import com.shade.lang.util.annotations.NotNull;

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
    public void compile(Context context, Assembler assembler) throws ScriptException {
        lhs.compile(context, assembler);
        rhs.compile(context, assembler);

        switch (operator) {
            case Add:
            case AddAssign:
                assembler.emit(Operation.ADD);
                break;
            case Sub:
            case SubAssign:
                assembler.emit(Operation.SUB);
                break;
            case Mul:
            case MulAssign:
                assembler.emit(Operation.MUL);
                break;
            case Div:
            case DivAssign:
                assembler.emit(Operation.DIV);
                break;
            case Eq:
                assembler.emit(Operation.CMP_EQ);
                break;
            case NotEq:
                assembler.emit(Operation.CMP_NE);
                break;
            case Less:
                assembler.emit(Operation.CMP_LT);
                break;
            case LessEq:
                assembler.emit(Operation.CMP_LE);
                break;
            case Greater:
                assembler.emit(Operation.CMP_GT);
                break;
            case GreaterEq:
                assembler.emit(Operation.CMP_GE);
                break;
            case Is:
                assembler.emit(Operation.INSTANCE_OF);
                break;
            default:
                throw new AssertionError("Unsupported binary operator: " + operator);
        }

        assembler.addLocation(getRegion().getBegin());
    }

    @NotNull
    @Override
    public Expression accept(@NotNull Visitor visitor) {
        if (visitor.enterBinaryExpression(this)) {
            final Expression lhs = this.lhs.accept(visitor);
            final Expression rhs = this.rhs.accept(visitor);

            if (lhs != this.lhs || rhs != this.rhs) {
                return visitor.leaveBinaryExpression(new BinaryExpression(lhs, rhs, operator, getRegion()));
            } else {
                return visitor.leaveBinaryExpression(this);
            }
        }

        return this;
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
