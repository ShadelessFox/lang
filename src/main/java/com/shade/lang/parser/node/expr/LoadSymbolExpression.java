package com.shade.lang.parser.node.expr;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.compiler.Opcode;
import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

public class LoadSymbolExpression extends Expression {
    private final String name;

    public LoadSymbolExpression(String name, Region region) {
        super(region);
        this.name = name;
    }

    @Override
    public void compile(Context context, Assembler assembler) {
        if (context.hasSlot(name)) {
            assembler.imm8(Opcode.GET_LOCAL);
            assembler.imm8(context.addSlot(name));
        } else {
            assembler.imm8(Opcode.GET_GLOBAL);
            assembler.imm32(assembler.addConstant(name));
        }
    }

    public String getName() {
        return name;
    }
}
