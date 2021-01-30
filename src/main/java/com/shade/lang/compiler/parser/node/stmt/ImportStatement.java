package com.shade.lang.compiler.parser.node.stmt;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.assembler.Operand;
import com.shade.lang.compiler.assembler.Operation;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Statement;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.token.Region;

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
        final String alias = this.alias == null ? this.name : this.alias;

        if (global) {
            assembler.emit(Operation.IMPORT, Operand.constant(name), Operand.imm8(Operand.UNDEFINED));
            assembler.addLocation(getRegion().getBegin());
            assembler.emit(Operation.SET_GLOBAL, Operand.constant(alias));
        } else {
            assembler.emit(Operation.IMPORT, Operand.constant(name), Operand.imm8(context.addSlot(alias)));
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
