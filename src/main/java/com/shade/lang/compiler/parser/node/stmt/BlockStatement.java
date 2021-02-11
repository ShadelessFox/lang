package com.shade.lang.compiler.parser.node.stmt;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Statement;
import com.shade.lang.compiler.parser.node.visitor.Visitor;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.util.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @NotNull
    @Override
    public Statement accept(@NotNull Visitor visitor) {
        if (visitor.enterBlockStatement(this)) {
            final List<Statement> statements = this.statements.stream()
                .map(x -> x.accept(visitor))
                .collect(Collectors.toList());

            if (!statements.equals(this.statements)) {
                return visitor.leaveBlockStatement(new BlockStatement(statements, getRegion()));
            } else {
                return visitor.leaveBlockStatement(this);
            }
        }

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockStatement that = (BlockStatement) o;
        return Objects.equals(statements, that.statements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statements);
    }

    public List<Statement> getStatements() {
        return statements;
    }
}
