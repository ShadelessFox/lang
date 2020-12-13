package com.shade.lang.parser.node.expr;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.compiler.Operand;
import com.shade.lang.compiler.Operation;
import com.shade.lang.parser.ScriptException;
import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

import java.util.List;
import java.util.Objects;

public class NewExpression extends Expression {
    private final Expression callee;
    private final List<Expression> arguments;

    public NewExpression(Expression callee, List<Expression> arguments, Region region) {
        super(region);
        this.callee = callee;
        this.arguments = arguments;
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        callee.compile(context, assembler);
        assembler.emit(Operation.NEW);
        assembler.emit(Operation.DUP);

        for (Expression argument : arguments) {
            argument.compile(context, assembler);
        }

        if (arguments.isEmpty()) {
            assembler.emit(Operation.DUP);
        } else {
            assembler.emit(Operation.DUP_AT, Operand.imm8(-arguments.size() - 1));
        }

        assembler.emit(Operation.GET_ATTRIBUTE, Operand.constant("<init>"));
        assembler.addLocation(getRegion().getBegin());

        assembler.emit(Operation.CALL, Operand.imm8(arguments.size() + 1));
        assembler.addLocation(getRegion().getBegin());

        assembler.emit(Operation.POP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewExpression that = (NewExpression) o;
        return callee.equals(that.callee) &&
            arguments.equals(that.arguments);
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
