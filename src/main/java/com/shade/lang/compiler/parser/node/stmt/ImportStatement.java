package com.shade.lang.compiler.parser.node.stmt;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.assembler.Operand;
import com.shade.lang.compiler.assembler.Operation;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Statement;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.node.visitor.Visitor;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.util.annotations.NotNull;
import com.shade.lang.util.annotations.Nullable;

public class ImportStatement extends Statement {
    private final String name;
    private final String alias;
    private final String[] items;
    private final boolean global;

    public ImportStatement(@NotNull String name, @Nullable String alias, @Nullable String[] items, boolean global, @NotNull Region region) {
        super(region);
        this.name = name;
        this.alias = alias;
        this.items = items;
        this.global = global;

        if (alias != null && items != null) {
            throw new IllegalArgumentException("Either 'alias' or 'items' may be specified");
        }
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        final String alias = this.alias == null ? this.name : this.alias;

        assembler.emit(Operation.IMPORT, Operand.constant(name));
        assembler.addLocation(getRegion().getBegin());

        if (items != null) {
            for (int index = 0, length = items.length; index < length; index++) {
                final String item = items[index];

                if (index < length - 1) {
                    // Emit DUP only for all items except last to avoid making POP at the end
                    assembler.emit(Operation.DUP);
                }

                assembler.emit(Operation.GET_ATTRIBUTE, Operand.constant(item));
                assembler.addLocation(getRegion().getBegin());

                if (global) {
                    assembler.emit(Operation.SET_GLOBAL, Operand.constant(item));
                } else {
                    assembler.emit(Operation.SET_LOCAL, Operand.imm8(context.addSlot(item)));
                }
            }
        } else {
            if (global) {
                assembler.emit(Operation.SET_GLOBAL, Operand.constant(alias));
            } else {
                assembler.emit(Operation.SET_LOCAL, Operand.imm8(context.addSlot(alias)));
            }
        }
    }

    @NotNull
    @Override
    public Statement accept(@NotNull Visitor visitor) {
        if (visitor.enterImportStatement(this)) {
            return visitor.leaveImportStatement(this);
        }

        return this;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @Nullable
    public String getAlias() {
        return alias;
    }

    @Nullable
    public String[] getItems() {
        return items;
    }

    public boolean isGlobal() {
        return global;
    }
}
