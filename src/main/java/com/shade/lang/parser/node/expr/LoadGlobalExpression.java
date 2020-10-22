package com.shade.lang.parser.node.expr;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.gen.Opcode;
import com.shade.lang.parser.node.Visitor;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.node.context.LocalContext;
import com.shade.lang.parser.token.Region;

public class LoadGlobalExpression implements Expression {
    private final String name;
    private final Region region;

    public LoadGlobalExpression(String name, Region region) {
        this.name = name;
        this.region = region;
    }

    public String getName() {
        return name;
    }

    @Override
    public Region getRegion() {
        return region;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void emit(Context context, Assembler assembler) {
        if (context instanceof LocalContext && ((LocalContext) context).hasSlot(name)) {
            LocalContext localContext = (LocalContext) context;
            assembler.imm8(Opcode.GET_LOCAL);
            assembler.imm8(localContext.getSlot(name));
        } else {
            assembler.imm8(Opcode.GET_GLOBAL);
            assembler.imm32(assembler.constant(name));
        }
    }
}
