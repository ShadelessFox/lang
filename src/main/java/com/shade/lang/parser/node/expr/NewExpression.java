package com.shade.lang.parser.node.expr;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.compiler.Opcode;
import com.shade.lang.parser.ScriptException;
import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

import java.util.List;
import java.util.Objects;

public class NewExpression extends Expression {
    private final LoadSymbolExpression callee;
    private final List<Expression> arguments;

    public NewExpression(String callee, List<Expression> arguments, Region region) {
        super(region);
        this.callee = new LoadSymbolExpression(callee, region);
        this.arguments = arguments;
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        callee.compile(context, assembler);
        assembler.imm8(Opcode.NEW);

        assembler.imm8(Opcode.DUP);

        for (Expression argument : arguments) {
            argument.compile(context, assembler);
        }

        if (arguments.isEmpty()) {
            assembler.imm8(Opcode.DUP);
        } else {
            assembler.imm8(Opcode.DUP_AT);
            assembler.imm8(-arguments.size() - 1);
        }

        assembler.imm8(Opcode.GET_ATTRIBUTE);
        assembler.imm32(assembler.addConstant("<init>"));
        assembler.imm8(Opcode.CALL);
        assembler.imm8(arguments.size() + 1);
        assembler.addTraceLine(getRegion().getBegin());
        assembler.imm8(Opcode.POP);
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

    public String getCallee() {
        return callee.getName();
    }

    public List<Expression> getArguments() {
        return arguments;
    }
}
