package com.shade.lang.parser.node.stmt;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.gen.Opcode;
import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.Statement;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

public class ReturnStatement extends Statement {
    private final Expression value;

    public ReturnStatement(Expression value, Region region) {
        super(region);
        this.value = value;
    }

    @Override
    public boolean isControlFlowReturned() {
        return true;
    }

    @Override
    public void compile(Context context, Assembler assembler) {
        value.compile(context, assembler);
        assembler.span(getRegion().getBegin());
        assembler.imm8(Opcode.RET);
    }

    public Expression getValue() {
        return value;
    }
}
