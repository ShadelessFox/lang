package com.shade.lang.parser.node.stmt;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.gen.Opcode;
import com.shade.lang.parser.node.Visitor;
import com.shade.lang.parser.node.expr.Expression;
import com.shade.lang.vm.runtime.Module;

public class ExpressionStatement implements Statement {
    private final Expression expression;

    public ExpressionStatement(Expression expression) {
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public boolean isControlFlowReturned() {
        return false;
    }

    @Override
    public void emit(Module module, Assembler assembler) {
        expression.emit(module, assembler);
        assembler.imm8(Opcode.POP);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
