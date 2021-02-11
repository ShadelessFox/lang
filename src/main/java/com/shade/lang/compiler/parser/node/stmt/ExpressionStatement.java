package com.shade.lang.compiler.parser.node.stmt;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.assembler.Operation;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Expression;
import com.shade.lang.compiler.parser.node.Statement;
import com.shade.lang.compiler.parser.node.visitor.Visitor;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.util.annotations.NotNull;

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

    @NotNull
    @Override
    public Statement accept(@NotNull Visitor visitor) {
        if (visitor.enterExpressionStatement(this)) {
            final Expression expression = this.expression.accept(visitor);

            if (expression != this.expression) {
                return visitor.leaveExpressionStatement(new ExpressionStatement(expression, getRegion()));
            } else {
                return visitor.leaveExpressionStatement(this);
            }
        }

        return this;
    }

    public Expression getExpression() {
        return expression;
    }
}
