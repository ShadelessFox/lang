package com.shade.lang.compiler.parser.node.expr;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.assembler.Operand;
import com.shade.lang.compiler.assembler.Operation;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Expression;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.node.visitor.Visitor;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.runtime.objects.value.NoneValue;
import com.shade.lang.util.annotations.NotNull;

public class UnwrapExpression extends Expression {
    private final Expression expression;

    public UnwrapExpression(@NotNull Expression expression, @NotNull Region region) {
        super(region);
        this.expression = expression;
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        expression.compile(context, assembler);
        assembler.emit(Operation.DUP);
        assembler.emit(Operation.PUSH, Operand.constant(NoneValue.INSTANCE));
        assembler.emit(Operation.CMP_EQ);
        Assembler.Label end = assembler.jump(Operation.JUMP_IF_FALSE);
        assembler.emit(Operation.RETURN);
        assembler.bind(end);
    }

    @NotNull
    @Override
    public Expression accept(@NotNull Visitor visitor) {
        if (visitor.enterUnwrapExpression(this)) {
            final Expression expression = this.expression.accept(visitor);

            if (expression != this.expression) {
                return visitor.leaveUnwrapExpression(new UnwrapExpression(expression, getRegion()));
            } else {
                return visitor.leaveUnwrapExpression(this);
            }
        }

        return this;
    }

    @NotNull
    public Expression getExpression() {
        return expression;
    }
}
