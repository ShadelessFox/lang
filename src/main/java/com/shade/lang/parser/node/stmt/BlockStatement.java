package com.shade.lang.parser.node.stmt;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.node.Visitor;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

import java.util.Collections;
import java.util.List;

public class BlockStatement implements Statement {
    private final List<Statement> statements;
    private final boolean controlFlowReturned;
    private final Region region;

    public BlockStatement(List<Statement> statements, Region region) {
        this.statements = Collections.unmodifiableList(statements);
        this.region = region;
        this.controlFlowReturned = this.statements.stream().anyMatch(Statement::isControlFlowReturned);
    }

    public List<Statement> getStatements() {
        return statements;
    }

    @Override
    public boolean isControlFlowReturned() {
        return controlFlowReturned;
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
        for (Statement statement : statements) {
            statement.emit(context, assembler);
        }
    }
}
