package com.shade.lang.compiler.parser.node.expr;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.assembler.Operand;
import com.shade.lang.compiler.assembler.Operation;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Expression;
import com.shade.lang.compiler.parser.node.context.ClassContext;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.token.Region;

import java.util.Collections;
import java.util.List;

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
            // TODO: This is a limitation for now. We don't know
            //       how many base classes does this class have.

            // TODO: Also we can't be sure if specified class
            //       is a parent of this class.

            throw new ScriptException("Super class must be explicitly specified", getRegion());
        }

        if (arguments != null) {
            // TODO: Constructor invocation. Looks ugly

            for (Expression argument : arguments) {
                argument.compile(context, assembler);
            }

            assembler.emit(Operation.GET_LOCAL, Operand.imm8(0));

            target.compile(context, assembler);
            assembler.emit(Operation.GET_ATTRIBUTE, Operand.constant("<init>"));
            assembler.addLocation(getRegion().getBegin());

            assembler.emit(Operation.CALL, Operand.imm8(arguments.size() + 1));
            assembler.addLocation(getRegion().getBegin());
        } else {
            // TODO: We must allow calling super class' functions (#37).
            //       This can be done by peeking inside target class'
            //       attributes and calling it with this class' instance.

            // TODO: The only problem is that we will need to inject
            //       this instance, because

            throw new ScriptException("Not implemented", getRegion());
        }
    }

    public Expression getTarget() {
        return target;
    }

    public List<Expression> getArguments() {
        return arguments;
    }
}
