package com.shade.lang.compiler.parser.node.stmt;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Statement;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.node.visitor.Visitor;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.util.annotations.NotNull;

public class EmptyStatement extends Statement {
    public static final EmptyStatement INSTANCE = new EmptyStatement();

    private EmptyStatement() {
        super(new Region(new Region.Span(0, 0, 0), new Region.Span(0, 0, 0)));
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
    }

    @NotNull
    @Override
    public Statement accept(@NotNull Visitor visitor) {
        return this;
    }

    public static boolean nonEmpty(@NotNull Statement statement) {
        return !(statement instanceof EmptyStatement);
    }
}
