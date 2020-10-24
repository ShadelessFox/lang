package com.shade.lang.parser.node.stmt;

import com.shade.lang.parser.ScriptException;
import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.node.Statement;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

import java.util.Collections;
import java.util.List;

public class BlockStatement extends Statement {
    private final List<Statement> statements;
    private final boolean controlFlowReturned;

    public BlockStatement(List<Statement> statements, Region region) {
        super(region);
        this.statements = Collections.unmodifiableList(statements);
        this.controlFlowReturned = this.statements.stream().anyMatch(Statement::isControlFlowReturned);
    }

    @Override
    public boolean isControlFlowReturned() {
        return controlFlowReturned;
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        for (Statement statement : statements) {
            statement.compile(context, assembler);
        }
    }

    public List<Statement> getStatements() {
        return statements;
    }
}
