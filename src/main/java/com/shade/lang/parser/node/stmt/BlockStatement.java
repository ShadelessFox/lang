package com.shade.lang.parser.node.stmt;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.parser.ScriptException;
import com.shade.lang.parser.node.Statement;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

import java.util.Collections;
import java.util.List;

public class BlockStatement extends Statement {
    private final List<Statement> statements;
    private final boolean controlFlowReturned;
    private final boolean controlFlowInterrupted;

    public BlockStatement(List<Statement> statements, Region region) {
        super(region);
        this.statements = Collections.unmodifiableList(statements);
        this.controlFlowReturned = this.statements.stream().anyMatch(Statement::isControlFlowReturned);
        this.controlFlowInterrupted = this.statements.stream().anyMatch(Statement::isControlFlowInterrupted);
    }

    @Override
    public boolean isControlFlowReturned() {
        return controlFlowReturned;
    }

    @Override
    public boolean isControlFlowInterrupted() {
        return controlFlowInterrupted;
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        try (Context inner = context.enter()) {
            for (Statement statement : statements) {
                statement.compile(inner, assembler);
            }
        }
    }

    public List<Statement> getStatements() {
        return statements;
    }
}
