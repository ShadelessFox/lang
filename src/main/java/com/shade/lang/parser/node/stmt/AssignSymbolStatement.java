package com.shade.lang.parser.node.stmt;

import com.shade.lang.parser.ScriptException;
import com.shade.lang.compiler.Assembler;
import com.shade.lang.compiler.Opcode;
import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.Statement;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

public class AssignSymbolStatement extends Statement {
    private final String name;
    private final Expression value;

    public AssignSymbolStatement(String name, Expression value, Region region) {
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
        value.compile(context, assembler);

        if (context.hasSlot(name)) {
            assembler.imm8(Opcode.SET_LOCAL);
            assembler.imm8(context.makeSlot(name));
        } else {
            assembler.imm8(Opcode.SET_GLOBAL);
            assembler.imm32(assembler.constant(name));
        }
    }

    public String getName() {
        return name;
    }

    public Expression getValue() {
        return value;
    }
}
