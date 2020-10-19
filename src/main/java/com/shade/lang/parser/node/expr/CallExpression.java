package com.shade.lang.parser.node.expr;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.gen.Opcode;
import com.shade.lang.parser.node.Visitor;
import com.shade.lang.vm.runtime.Module;

import java.util.Collections;
import java.util.List;

public class CallExpression implements Expression {
    private final Expression callee;
    private final List<Expression> arguments;

    public CallExpression(Expression callee, List<Expression> arguments) {
        this.callee = callee;
        this.arguments = Collections.unmodifiableList(arguments);
    }

    public Expression getCallee() {
        return callee;
    }

    public List<Expression> getArguments() {
        return arguments;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void emit(Module module, Assembler assembler) {
        for (Expression argument : arguments) {
            argument.emit(module, assembler);
        }

        callee.emit(module, assembler);

        assembler.imm8(Opcode.CALL);
        assembler.imm8((byte) arguments.size());
    }
}
