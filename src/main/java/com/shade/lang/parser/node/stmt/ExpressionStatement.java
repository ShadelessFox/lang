package com.shade.lang.parser.node.stmt;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.compiler.Operation;
import com.shade.lang.parser.ScriptException;
import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.Statement;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

public class ExpressionStatement extends Statement {
    private final Expression expression;

    public ExpressionStatement(Expression expression, Region region) {
        super(region);
        this.expression = expression;
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        expression.compile(context, assembler);
        assembler.emit(Operation.POP);
    }

    public Expression getExpression() {
        return expression;
    }
}
