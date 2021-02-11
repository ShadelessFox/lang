package com.shade.lang.compiler.parser.node.expr;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.assembler.Operand;
import com.shade.lang.compiler.assembler.Operation;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Expression;
import com.shade.lang.compiler.parser.node.visitor.Visitor;
import com.shade.lang.compiler.parser.node.context.ClassContext;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.util.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SuperExpression extends Expression {
    private final Expression target;
    private final List<Expression> arguments;

    public SuperExpression(Expression target, List<Expression> arguments, Region region) {
        super(region);
        this.target = target;
        this.arguments = arguments == null ? null : Collections.unmodifiableList(arguments);
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        final ClassContext classContext = context.unwrap(ClassContext.class);

        if (classContext == null) {
            throw new ScriptException("Cannot use 'super' outside class declaration", getRegion());
        }

        if (target == null) {
            throw new ScriptException("Super class must be explicitly specified", getRegion());
        }

        target.compile(context, assembler);
        assembler.emit(Operation.GET_LOCAL, Operand.imm8(0));
        assembler.emit(Operation.SUPER);

        if (arguments != null) {
            for (Expression argument : arguments) {
                argument.compile(context, assembler);
            }

            assembler.emit(Operation.DUP_AT, Operand.imm8(-arguments.size() - 1));
            assembler.emit(Operation.GET_ATTRIBUTE, Operand.constant("<init>"));
            assembler.emit(Operation.CALL, Operand.imm8(arguments.size() + 1));
        }
    }

    @NotNull
    @Override
    public Expression accept(@NotNull Visitor visitor) {
        if (visitor.enterSuperExpression(this)) {
            final Expression target = this.target.accept(visitor);
            final List<Expression> arguments = this.arguments.stream()
                .map(x -> x.accept(visitor))
                .collect(Collectors.toList());

            if (target != this.target || !arguments.equals(this.arguments)) {
                return visitor.leaveSuperExpression(new SuperExpression(target, arguments, getRegion()));
            } else {
                return visitor.leaveSuperExpression(this);
            }
        }

        return this;
    }

    public Expression getTarget() {
        return target;
    }

    public List<Expression> getArguments() {
        return arguments;
    }
}
