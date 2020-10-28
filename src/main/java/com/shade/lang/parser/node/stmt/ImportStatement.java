package com.shade.lang.parser.node.stmt;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.parser.node.Statement;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

public class ImportStatement extends Statement {
    private final String name;
    private final String alias;
    private final boolean path;

    public ImportStatement(String name, String alias, boolean path, Region region) {
        super(region);
        this.name = name;
        this.alias = alias;
        this.path = path;
    }

    @Override
    public void compile(Context context, Assembler assembler) {
        context.getModule().getImports().add(this);
    }

    public String getName() {
        return name;
    }

    public String getAlias() {
        return alias;
    }

    public boolean isPath() {
        return path;
    }
}
