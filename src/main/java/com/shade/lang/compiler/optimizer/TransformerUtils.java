package com.shade.lang.compiler.optimizer;

import com.shade.lang.compiler.parser.node.Expression;
import com.shade.lang.compiler.parser.node.expr.LoadConstantExpression;

public final class TransformerUtils {
    private TransformerUtils() {
    }

    public static <T> boolean isConst(Expression expression, Class<T> clazz) {
        return expression instanceof LoadConstantExpression<?> && clazz.isInstance(((LoadConstantExpression<?>) expression).getValue());
    }

    public static <T> T asConst(Expression expression, Class<T> clazz) {
        return clazz.cast(((LoadConstantExpression<?>) expression).getValue());
    }
}
