package com.shade.lang.parser.node.expr;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.parser.ScriptException;
import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

public class CompoundExpression extends Expression {
    private final Expression expression;

    public CompoundExpression(Expression expression, Region region) {
        super(region);
        this.expression = expression;
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        expression.compile(context, assembler);
    }

    public Expression getExpression() {
        return expression;
    }
}
