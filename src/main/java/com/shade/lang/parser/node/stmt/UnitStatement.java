package com.shade.lang.parser.node.stmt;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.parser.ScriptException;
import com.shade.lang.parser.node.Statement;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

import java.util.Collections;
import java.util.List;

public class UnitStatement extends Statement {
    private final String name;
    private final List<Statement> statements;

    public UnitStatement(String name, List<Statement> statements, Region region) {
        super(region);
        this.name = name;
        this.statements = Collections.unmodifiableList(statements);
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        for (Statement statement : statements) {
            statement.compile(context, assembler);
        }
    }

    public String getName() {
        return name;
    }

    public List<Statement> getStatements() {
        return statements;
    }
}
