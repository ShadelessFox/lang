package com.shade.lang.parser.node.stmt;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.gen.Opcode;
import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.Statement;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.node.context.LocalContext;
import com.shade.lang.parser.token.Region;

public class AssignGlobalStatement extends Statement {
    private final String name;
    private final Expression value;

    public AssignGlobalStatement(String name, Expression value, Region region) {
        super(region);
        this.name = name;
        this.value = value;
    }

    @Override
    public boolean isControlFlowReturned() {
        return false;
    }

    @Override
    public void compile(Context context, Assembler assembler) {
        value.compile(context, assembler);

        if (context instanceof LocalContext && ((LocalContext) context).hasSlot(name)) {
            LocalContext localContext = (LocalContext) context;
            assembler.imm8(Opcode.SET_LOCAL);
            assembler.imm8(localContext.getSlot(name));
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
