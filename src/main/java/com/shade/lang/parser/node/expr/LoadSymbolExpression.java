package com.shade.lang.parser.node.expr;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.gen.Opcode;
import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.node.context.LocalContext;
import com.shade.lang.parser.token.Region;

public class LoadSymbolExpression extends Expression {
    private final String name;

    public LoadSymbolExpression(String name, Region region) {
        super(region);
        this.name = name;
    }

    @Override
    public void compile(Context context, Assembler assembler) {
        if (context instanceof LocalContext && ((LocalContext) context).hasSlot(name)) {
            LocalContext localContext = (LocalContext) context;
            assembler.imm8(Opcode.GET_LOCAL);
            assembler.imm8(localContext.getSlot(name));
        } else {
            assembler.imm8(Opcode.GET_GLOBAL);
            assembler.imm32(assembler.constant(name));
        }

        assembler.span(getRegion().getBegin());
    }

    public String getName() {
        return name;
    }
}
