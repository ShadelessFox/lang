package com.shade.lang.compiler.parser.node.expr;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Expression;
import com.shade.lang.compiler.parser.node.visitor.Visitor;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.util.annotations.NotNull;

import java.util.Objects;

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

    @NotNull
    @Override
    public Expression accept(@NotNull Visitor visitor) {
        if (visitor.enterCompoundExpression(this)) {
            final Expression expression = this.expression.accept(visitor);

            if (expression != this.expression) {
                return visitor.leaveCompoundExpression(new CompoundExpression(expression, getRegion()));
            } else {
                return visitor.leaveCompoundExpression(this);
            }
        }

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompoundExpression that = (CompoundExpression) o;
        return expression.equals(that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression);
    }

    public Expression getExpression() {
        return expression;
    }
}
