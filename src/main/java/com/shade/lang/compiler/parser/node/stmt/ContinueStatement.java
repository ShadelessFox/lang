package com.shade.lang.compiler.parser.node.stmt;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.assembler.Operation;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Statement;
import com.shade.lang.compiler.parser.node.visitor.Visitor;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.node.context.LoopContext;
import com.shade.lang.compiler.parser.node.context.FinallyContext;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.util.annotations.NotNull;

public class ContinueStatement extends Statement {
    private final String name;

    public ContinueStatement(String name, Region region) {
        super(region);
        this.name = name;
    }

    @Override
    public boolean isControlFlowInterrupted() {
        return true;
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        final FinallyContext finallyContext = context.unwrap(FinallyContext.class);
        if (finallyContext != null) {
            finallyContext.compile(assembler);
        }

        final LoopContext loopContext = context.unwrap(LoopContext.class);
        if (loopContext == null) {
            throw new ScriptException("Cannot use 'continue' outside loop statement", getRegion());
        }

        loopContext.addCanceller(this, assembler.jump(Operation.JUMP), LoopContext.CancelType.Continue, name);
    }

    @NotNull
    @Override
    public Statement accept(@NotNull Visitor visitor) {
        if (visitor.enterContinueStatement(this)) {
            return visitor.leaveContinueStatement(this);
        }

        return this;
    }

    public String getName() {
        return name;
    }
}
