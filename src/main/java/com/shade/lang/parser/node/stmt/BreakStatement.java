package com.shade.lang.parser.node.stmt;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.compiler.Opcode;
import com.shade.lang.parser.ScriptException;
import com.shade.lang.parser.node.Statement;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.node.context.LoopContext;
import com.shade.lang.parser.token.Region;

public class BreakStatement extends Statement {
    public BreakStatement(Region region) {
        super(region);
    }

    @Override
    public boolean isControlFlowInterrupted() {
        return true;
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        LoopContext loopContext = context.unwrap(LoopContext.class);

        if (loopContext == null) {
            throw new ScriptException("Cannot use 'break' outside loop statement", getRegion());
        }

        loopContext.addCanceller(assembler.jump(Opcode.JUMP), LoopContext.CancelType.Break);
    }
}
