package com.shade.lang.compiler.optimizer.transformers;

import com.shade.lang.compiler.optimizer.SimpleTransformer;
import com.shade.lang.compiler.parser.node.Expression;
import com.shade.lang.compiler.parser.node.expr.*;
import com.shade.lang.compiler.parser.token.TokenKind;

public class ConstantFoldingTransformer extends SimpleTransformer {
    @Override
    public int getLevel() {
        return 1;
    }

    @Override
    public Expression transform(BinaryExpression expression) {
        BinaryExpression transformed = (BinaryExpression) super.transform(expression);

        if (isConst(transformed.getLhs(), Number.class) && isConst(transformed.getRhs(), Number.class)) {
            Number lhsValue = asConst(transformed.getLhs());
            Number rhsValue = asConst(transformed.getRhs());

            if (lhsValue instanceof Float || rhsValue instanceof Float) {
                switch (transformed.getOperator()) {
                    case Add:
                        return new LoadConstantExpression<>(lhsValue.floatValue() + rhsValue.floatValue(), transformed.getRegion());
                    case Sub:
                        return new LoadConstantExpression<>(lhsValue.floatValue() - rhsValue.floatValue(), transformed.getRegion());
                    case Mul:
                        return new LoadConstantExpression<>(lhsValue.floatValue() * rhsValue.floatValue(), transformed.getRegion());
                    case Div:
                        return new LoadConstantExpression<>(lhsValue.floatValue() / rhsValue.floatValue(), transformed.getRegion());
                    case Eq:
                        return new LoadConstantExpression<>(lhsValue.floatValue() == rhsValue.floatValue(), transformed.getRegion());
                    case NotEq:
                        return new LoadConstantExpression<>(lhsValue.floatValue() != rhsValue.floatValue(), transformed.getRegion());
                    case Less:
                        return new LoadConstantExpression<>(lhsValue.floatValue() < rhsValue.floatValue(), transformed.getRegion());
                    case LessEq:
                        return new LoadConstantExpression<>(lhsValue.floatValue() <= rhsValue.floatValue(), transformed.getRegion());
                    case Greater:
                        return new LoadConstantExpression<>(lhsValue.floatValue() > rhsValue.floatValue(), transformed.getRegion());
                    case GreaterEq:
                        return new LoadConstantExpression<>(lhsValue.floatValue() >= rhsValue.floatValue(), transformed.getRegion());
                }
            } else {
                switch (transformed.getOperator()) {
                    case Add:
                        return new LoadConstantExpression<>(lhsValue.intValue() + rhsValue.intValue(), transformed.getRegion());
                    case Sub:
                        return new LoadConstantExpression<>(lhsValue.intValue() - rhsValue.intValue(), transformed.getRegion());
                    case Mul:
                        return new LoadConstantExpression<>(lhsValue.intValue() * rhsValue.intValue(), transformed.getRegion());
                    case Div:
                        return new LoadConstantExpression<>(lhsValue.intValue() / rhsValue.intValue(), transformed.getRegion());
                    case Eq:
                        return new LoadConstantExpression<>(lhsValue.intValue() == rhsValue.intValue(), transformed.getRegion());
                    case NotEq:
                        return new LoadConstantExpression<>(lhsValue.intValue() != rhsValue.intValue(), transformed.getRegion());
                    case Less:
                        return new LoadConstantExpression<>(lhsValue.intValue() < rhsValue.intValue(), transformed.getRegion());
                    case LessEq:
                        return new LoadConstantExpression<>(lhsValue.intValue() <= rhsValue.intValue(), transformed.getRegion());
                    case Greater:
                        return new LoadConstantExpression<>(lhsValue.intValue() > rhsValue.intValue(), transformed.getRegion());
                    case GreaterEq:
                        return new LoadConstantExpression<>(lhsValue.intValue() >= rhsValue.intValue(), transformed.getRegion());
                }
            }
        }

        if (isConst(transformed.getLhs(), Boolean.class) && isConst(transformed.getRhs(), Boolean.class)) {
            boolean lhsValue = asConst(transformed.getLhs());
            boolean rhsValue = asConst(transformed.getRhs());

            switch (transformed.getOperator()) {
                case Eq:
                    return new LoadConstantExpression<>(lhsValue == rhsValue, transformed.getRegion());
                case NotEq:
                    return new LoadConstantExpression<>(lhsValue != rhsValue, transformed.getRegion());
            }
        }

        if (isConst(transformed.getLhs(), String.class) && isConst(transformed.getRhs(), String.class) && transformed.getOperator() == TokenKind.Add) {
            String lhsValue = asConst(transformed.getLhs());
            String rhsValue = asConst(transformed.getRhs());

            return new LoadConstantExpression<>(lhsValue + rhsValue, transformed.getRegion());
        }

        return transformed;
    }

    @Override
    public Expression transform(LogicalExpression expression) {
        LogicalExpression transformed = (LogicalExpression) super.transform(expression);

        if (isConst(transformed.getLhs(), Boolean.class) && isConst(transformed.getRhs(), Boolean.class)) {
            boolean lhsValue = asConst(transformed.getLhs());
            boolean rhsValue = asConst(transformed.getRhs());

            switch (transformed.getOperator()) {
                case And:
                    return new LoadConstantExpression<>(lhsValue && rhsValue, transformed.getRegion());
                case Or:
                    return new LoadConstantExpression<>(lhsValue || rhsValue, transformed.getRegion());
            }
        }

        return transformed;
    }

    @Override
    public Expression transform(UnaryExpression expression) {
        UnaryExpression transformed = (UnaryExpression) super.transform(expression);

        if (isConst(transformed.getRhs(), Number.class) && transformed.getOperator() == TokenKind.Sub) {
            Number rhsValue = asConst(transformed.getRhs());

            if (rhsValue instanceof Float) {
                return new LoadConstantExpression<>(-rhsValue.floatValue(), transformed.getRegion());
            } else {
                return new LoadConstantExpression<>(-rhsValue.intValue(), transformed.getRegion());
            }
        }

        if (isConst(transformed.getRhs(), Boolean.class) && transformed.getOperator() == TokenKind.Not) {
            boolean rhsValue = asConst(transformed.getRhs());
            return new LoadConstantExpression<>(!rhsValue, transformed.getRegion());
        }

        return transformed;
    }
}
