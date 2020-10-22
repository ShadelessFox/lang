package com.shade.lang.parser.node.stmt;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.node.Visitor;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

import java.util.Collections;
import java.util.List;

public class UnitStatement implements Statement {
    private final String name;
    private final List<Statement> statements;
    private final Region region;

    public UnitStatement(String name, List<Statement> statements, Region region) {
        this.name = name;
        this.statements = Collections.unmodifiableList(statements);
        this.region = region;
    }

    public String getName() {
        return name;
    }

    public List<Statement> getStatements() {
        return statements;
    }

    @Override
    public boolean isControlFlowReturned() {
        return false;
    }

    @Override
    public Region getRegion() {
        return region;
    }

    @Override
    public void emit(Context context, Assembler assembler) {
        statements.forEach(x -> x.emit(context, assembler));
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
