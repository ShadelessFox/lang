package com.shade.lang.parser.node.stmt;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.node.Statement;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

public class ImportStatement extends Statement {
    private final String name;
    private final boolean path;

    public ImportStatement(String name, boolean path, Region region) {
        super(region);
        this.name = name;
        this.path = path;
    }

    @Override
    public boolean isControlFlowReturned() {
        return false;
    }

    @Override
    public void compile(Context context, Assembler assembler) {
        context.getModule().getImports().add(this);
    }

    public String getName() {
        return name;
    }

    public boolean isPath() {
        return path;
    }
}
