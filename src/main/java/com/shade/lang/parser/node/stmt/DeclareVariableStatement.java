package com.shade.lang.parser.node.stmt;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.node.Visitor;
import com.shade.lang.parser.node.expr.Expression;
import com.shade.lang.vm.runtime.Module;

public class DeclareVariableStatement implements Statement {
    private final String name;
    private final Expression value;

    public DeclareVariableStatement(String name, Expression value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void emit(Module module, Assembler assembler) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean isControlFlowReturned() {
        return false;
    }
}
