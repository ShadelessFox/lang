package com.shade.lang.parser.node.stmt;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.node.Visitor;
import com.shade.lang.vm.runtime.Module;

import java.util.Collections;
import java.util.List;

public class BlockStatement implements Statement {
    private final List<Statement> statements;
    private final boolean controlFlowReturned;

    public BlockStatement(List<Statement> statements) {
        this.statements = Collections.unmodifiableList(statements);
        this.controlFlowReturned = this.statements.stream().anyMatch(Statement::isControlFlowReturned);
    }

    public List<Statement> getStatements() {
        return statements;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void emit(Module module, Assembler assembler) {
        for (Statement statement : statements) {
            statement.emit(module, assembler);
        }
    }

    @Override
    public boolean isControlFlowReturned() {
        return controlFlowReturned;
    }
}
