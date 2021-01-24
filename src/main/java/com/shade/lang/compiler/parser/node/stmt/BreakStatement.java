package com.shade.lang.compiler.parser.node.stmt;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.assembler.Operation;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Statement;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.node.context.LoopContext;
import com.shade.lang.compiler.parser.token.Region;

public class BreakStatement extends Statement {
    private final String name;

    public BreakStatement(String name, Region region) {
        super(region);
        this.name = name;
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

        loopContext.addCanceller(this, assembler.jump(Operation.JUMP), LoopContext.CancelType.Break, name);
    }

    public String getName() {
        return name;
    }
}