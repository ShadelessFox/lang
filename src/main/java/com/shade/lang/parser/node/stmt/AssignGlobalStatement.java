package com.shade.lang.parser.node.stmt;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.gen.Opcode;
import com.shade.lang.parser.node.Visitor;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.node.context.LocalContext;
import com.shade.lang.parser.node.expr.Expression;
import com.shade.lang.parser.token.Region;

public class AssignGlobalStatement implements Statement {
    private final String name;
    private final Expression value;
    private final Region region;

    public AssignGlobalStatement(String name, Expression value, Region region) {
        this.name = name;
        this.value = value;
        this.region = region;
    }

    public String getName() {
        return name;
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public boolean isControlFlowReturned() {
        return false;
    }

    @Override
    public Region getRegion() {
        return region;
    }

    @Override
    public void emit(Context context, Assembler assembler) {
        value.emit(context, assembler);

        if (context instanceof LocalContext && ((LocalContext) context).hasSlot(name)) {
            LocalContext localContext = (LocalContext) context;
            assembler.imm8(Opcode.SET_LOCAL);
            assembler.imm8(localContext.getSlot(name));
        } else {
            assembler.imm8(Opcode.SET_GLOBAL);
            assembler.imm32(assembler.constant(name));
        }
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
