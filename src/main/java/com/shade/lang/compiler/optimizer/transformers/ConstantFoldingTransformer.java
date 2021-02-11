package com.shade.lang.compiler.optimizer.transformers;

import com.shade.lang.compiler.optimizer.Transformer;
import com.shade.lang.compiler.optimizer.TransformerProvider;
import com.shade.lang.compiler.parser.node.Expression;
import com.shade.lang.compiler.parser.node.expr.BinaryExpression;
import com.shade.lang.compiler.parser.node.expr.LoadConstantExpression;
import com.shade.lang.compiler.parser.node.expr.LogicalExpression;
import com.shade.lang.compiler.parser.node.expr.UnaryExpression;
import com.shade.lang.compiler.parser.token.TokenKind;
import com.shade.lang.util.annotations.NotNull;

import static com.shade.lang.compiler.optimizer.TransformerUtils.asConst;
import static com.shade.lang.compiler.optimizer.TransformerUtils.isConst;

public class ConstantFoldingTransformer extends Transformer implements TransformerProvider {
    private static final ConstantFoldingTransformer INSTANCE = new ConstantFoldingTransformer();

    @Override
    public int getLevel() {
        return 1;
    }

    @NotNull
    @Override
    public Transformer create() {
        return INSTANCE;
    }

    @NotNull
    @Override
    public Expression leaveBinaryExpression(@NotNull BinaryExpression expression) {
        if (isConst(expression.getLhs(), Number.class) && isConst(expression.getRhs(), Number.class)) {
            final Number lhs = asConst(expression.getLhs(), Number.class);
            final Number rhs = asConst(expression.getRhs(), Number.class);

            if (lhs instanceof Float || rhs instanceof Float) {
                switch (expression.getOperator()) {
                    case Add:
                        return new LoadConstantExpression<>(lhs.floatValue() + rhs.floatValue(), expression.getRegion());
                    case Sub:
                        return new LoadConstantExpression<>(lhs.floatValue() - rhs.floatValue(), expression.getRegion());
                    case Mul:
                        return new LoadConstantExpression<>(lhs.floatValue() * rhs.floatValue(), expression.getRegion());
                    case Div:
                        return new LoadConstantExpression<>(lhs.floatValue() / rhs.floatValue(), expression.getRegion());
                    case Eq:
                        return new LoadConstantExpression<>(lhs.floatValue() == rhs.floatValue(), expression.getRegion());
                    case NotEq:
                        return new LoadConstantExpression<>(lhs.floatValue() != rhs.floatValue(), expression.getRegion());
                    case Less:
                        return new LoadConstantExpression<>(lhs.floatValue() < rhs.floatValue(), expression.getRegion());
                    case LessEq:
                        return new LoadConstantExpression<>(lhs.floatValue() <= rhs.floatValue(), expression.getRegion());
                    case Greater:
                        return new LoadConstantExpression<>(lhs.floatValue() > rhs.floatValue(), expression.getRegion());
                    case GreaterEq:
                        return new LoadConstantExpression<>(lhs.floatValue() >= rhs.floatValue(), expression.getRegion());
                }
            } else if (lhs instanceof Integer || rhs instanceof Integer) {
                switch (expression.getOperator()) {
                    case Add:
                        return new LoadConstantExpression<>(lhs.intValue() + rhs.intValue(), expression.getRegion());
                    case Sub:
                        return new LoadConstantExpression<>(lhs.intValue() - rhs.intValue(), expression.getRegion());
                    case Mul:
                        return new LoadConstantExpression<>(lhs.intValue() * rhs.intValue(), expression.getRegion());
                    case Div:
                        return new LoadConstantExpression<>(lhs.intValue() / rhs.intValue(), expression.getRegion());
                    case Eq:
                        return new LoadConstantExpression<>(lhs.intValue() == rhs.intValue(), expression.getRegion());
                    case NotEq:
                        return new LoadConstantExpression<>(lhs.intValue() != rhs.intValue(), expression.getRegion());
                    case Less:
                        return new LoadConstantExpression<>(lhs.intValue() < rhs.intValue(), expression.getRegion());
                    case LessEq:
                        return new LoadConstantExpression<>(lhs.intValue() <= rhs.intValue(), expression.getRegion());
                    case Greater:
                        return new LoadConstantExpression<>(lhs.intValue() > rhs.intValue(), expression.getRegion());
                    case GreaterEq:
                        return new LoadConstantExpression<>(lhs.intValue() >= rhs.intValue(), expression.getRegion());
                }
            }
        }

        if (isConst(expression.getLhs(), Boolean.class) && isConst(expression.getRhs(), Boolean.class)) {
            final Boolean lhs = asConst(expression.getLhs(), Boolean.class);
            final Boolean rhs = asConst(expression.getRhs(), Boolean.class);

            switch (expression.getOperator()) {
                case Eq:
                    return new LoadConstantExpression<>(lhs == rhs, expression.getRegion());
                case NotEq:
                    return new LoadConstantExpression<>(lhs != rhs, expression.getRegion());
            }
        }

        if (isConst(expression.getLhs(), String.class) && isConst(expression.getRhs(), String.class) && expression.getOperator() == TokenKind.Add) {
            final String lhs = asConst(expression.getLhs(), String.class);
            final String rhs = asConst(expression.getRhs(), String.class);

            return new LoadConstantExpression<>(lhs + rhs, expression.getRegion());
        }

        return super.leaveBinaryExpression(expression);
    }

    @NotNull
    @Override
    public Expression leaveLogicalExpression(@NotNull LogicalExpression expression) {
        if (isConst(expression.getLhs(), Boolean.class) && isConst(expression.getRhs(), Boolean.class)) {
            final Boolean lhs = asConst(expression.getLhs(), Boolean.class);
            final Boolean rhs = asConst(expression.getRhs(), Boolean.class);

            switch (expression.getOperator()) {
                case And:
                    return new LoadConstantExpression<>(lhs && rhs, expression.getRegion());
                case Or:
                    return new LoadConstantExpression<>(lhs || rhs, expression.getRegion());
            }
        }

        return super.leaveLogicalExpression(expression);
    }

    @NotNull
    @Override
    public Expression leaveUnaryExpression(@NotNull UnaryExpression expression) {
        if (isConst(expression.getRhs(), Number.class) && expression.getOperator() == TokenKind.Sub) {
            final Number rhs = asConst(expression.getRhs(), Number.class);

            if (rhs instanceof Float) {
                return new LoadConstantExpression<>(-rhs.floatValue(), expression.getRegion());
            } else if (rhs instanceof Integer) {
                return new LoadConstantExpression<>(-rhs.intValue(), expression.getRegion());
            }
        }

        if (isConst(expression.getRhs(), Boolean.class) && expression.getOperator() == TokenKind.Not) {
            final Boolean rhs = asConst(expression.getRhs(), Boolean.class);
            return new LoadConstantExpression<>(!rhs, expression.getRegion());
        }

        return super.leaveUnaryExpression(expression);
    }
}
