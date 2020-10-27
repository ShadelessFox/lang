package com.shade.lang.parser.node.stmt;

import com.shade.lang.parser.ScriptException;
import com.shade.lang.compiler.Assembler;
import com.shade.lang.compiler.Opcode;
import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.Statement;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

public class DeclareVariableStatement extends Statement {
    private final String name;
    private final Expression value;

    public DeclareVariableStatement(String name, Expression value, Region region) {
        super(region);
        this.name = name;
        this.value = value;
    }

    @Override
    public boolean isControlFlowReturned() {
        return false;
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        if (!context.hasSlot(name)) {
            int slot = context.addSlot(name);
            value.compile(context, assembler);
            assembler.imm8(Opcode.SET_LOCAL);
            assembler.imm8(slot);
        } else {
            throw new ScriptException("Local variable '" + name + "' already declared", getRegion());
        }
    }

    public String getName() {
        return name;
    }

    public Expression getValue() {
        return value;
    }
}
