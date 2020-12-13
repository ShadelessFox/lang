package com.shade.lang.parser.node.stmt;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.compiler.Operand;
import com.shade.lang.compiler.Operation;
import com.shade.lang.parser.ScriptException;
import com.shade.lang.parser.node.Statement;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

public class ImportStatement extends Statement {
    private final String name;
    private final String alias;
    private final boolean global;

    public ImportStatement(String name, String alias, boolean global, Region region) {
        super(region);
        this.name = name;
        this.alias = alias;
        this.global = global;
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        if (global) {
            context.getModule().getImports().add(this);
        } else {
            assembler.emit(Operation.IMPORT, Operand.constant(name), Operand.imm8(context.addSlot(alias == null ? name : alias)));
            assembler.addLocation(getRegion().getBegin());
        }
    }

    public String getName() {
        return name;
    }

    public String getAlias() {
        return alias;
    }

    public boolean isGlobal() {
        return global;
    }
}
