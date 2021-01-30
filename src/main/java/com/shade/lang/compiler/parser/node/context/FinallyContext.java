package com.shade.lang.compiler.parser.node.context;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.stmt.BlockStatement;
import com.shade.lang.compiler.parser.node.stmt.TryStatement;
import com.shade.lang.util.annotations.NotNull;

public class FinallyContext extends Context {
    private final TryStatement statement;

    public FinallyContext(@NotNull Context parent, TryStatement statement) {
        super(parent);
        this.statement = statement;
    }

    public void compile(@NotNull Assembler assembler) throws ScriptException {
        final BlockStatement body = statement.getFinallyBody();

        if (body != null) {
            body.compile(parent, assembler);
        }
    }

    public TryStatement getStatement() {
        return statement;
    }
}
