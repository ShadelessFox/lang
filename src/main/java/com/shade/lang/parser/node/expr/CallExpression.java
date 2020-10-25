package com.shade.lang.parser.node.expr;

import com.shade.lang.parser.ScriptException;
import com.shade.lang.compiler.Assembler;
import com.shade.lang.compiler.Opcode;
import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

import java.util.Collections;
import java.util.List;

public class CallExpression extends Expression {
    private final Expression callee;
    private final List<Expression> arguments;
    private final Region region;

    public CallExpression(Expression callee, List<Expression> arguments, Region region) {
        super(region);
        this.callee = callee;
        this.arguments = Collections.unmodifiableList(arguments);
        this.region = region;
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        for (Expression argument : arguments) {
            argument.compile(context, assembler);
        }

        callee.compile(context, assembler);

        assembler.imm8(Opcode.CALL);
        assembler.imm8((byte) arguments.size());
        assembler.span(region.getBegin());
    }

    public Expression getCallee() {
        return callee;
    }

    public List<Expression> getArguments() {
        return arguments;
    }
}
