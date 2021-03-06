package com.shade.lang.compiler.parser.node.expr;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.assembler.Operand;
import com.shade.lang.compiler.assembler.Operation;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Expression;
import com.shade.lang.compiler.parser.node.visitor.Visitor;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.util.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CallExpression extends Expression {
    private final Expression callee;
    private final List<Expression> arguments;

    public CallExpression(Expression callee, List<Expression> arguments, Region region) {
        super(region);
        this.callee = callee;
        this.arguments = Collections.unmodifiableList(arguments);
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        for (Expression argument : arguments) {
            argument.compile(context, assembler);
        }

        callee.compile(context, assembler);

        assembler.emit(Operation.CALL, Operand.imm8(arguments.size()));
        assembler.addLocation(getRegion().getBegin());
    }

    @NotNull
    @Override
    public Expression accept(@NotNull Visitor visitor) {
        if (visitor.enterCallExpression(this)) {
            final Expression callee = this.callee.accept(visitor);
            final List<Expression> arguments = this.arguments.stream()
                .map(x -> x.accept(visitor))
                .collect(Collectors.toList());

            if (callee != this.callee || !arguments.equals(this.arguments)) {
                return visitor.leaveCallExpression(new CallExpression(callee, arguments, getRegion()));
            } else {
                return visitor.leaveCallExpression(this);
            }
        }

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CallExpression that = (CallExpression) o;
        return callee.equals(that.callee) && arguments.equals(that.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(callee, arguments);
    }

    public Expression getCallee() {
        return callee;
    }

    public List<Expression> getArguments() {
        return arguments;
    }
}
