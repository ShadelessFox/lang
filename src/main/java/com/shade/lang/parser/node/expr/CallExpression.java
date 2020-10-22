package com.shade.lang.parser.node.expr;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.gen.Opcode;
import com.shade.lang.parser.node.Visitor;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

import java.util.Collections;
import java.util.List;

public class CallExpression implements Expression {
    private final Expression callee;
    private final List<Expression> arguments;
    private final Region region;

    public CallExpression(Expression callee, List<Expression> arguments, Region region) {
        this.callee = callee;
        this.arguments = Collections.unmodifiableList(arguments);
        this.region = region;
    }

    public Expression getCallee() {
        return callee;
    }

    public List<Expression> getArguments() {
        return arguments;
    }

    @Override
    public Region getRegion() {
        return region;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void emit(Context context, Assembler assembler) {
        for (Expression argument : arguments) {
            argument.emit(context, assembler);
        }

        callee.emit(context, assembler);

        assembler.span(region.getBegin());
        assembler.imm8(Opcode.CALL);
        assembler.imm8((byte) arguments.size());
    }
}
