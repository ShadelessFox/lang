package com.shade.lang.parser.node.stmt;

import com.shade.lang.parser.ScriptException;
import com.shade.lang.compiler.Assembler;
import com.shade.lang.compiler.Opcode;
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
    public void compile(Context context, Assembler assembler) throws ScriptException {
        assembler.addDebugLine(getRegion().getBegin(), "Return");
        value.compile(context, assembler);
        assembler.imm8(Opcode.RET);
    }

    public Expression getValue() {
        return value;
    }
}
