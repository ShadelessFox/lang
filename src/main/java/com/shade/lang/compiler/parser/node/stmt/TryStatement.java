package com.shade.lang.compiler.parser.node.stmt;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.assembler.Operation;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Statement;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.node.context.FunctionContext;
import com.shade.lang.compiler.parser.token.Region;

public class TryStatement extends Statement {
    private final BlockStatement body;
    private final BlockStatement recover;
    private final String name;

    public TryStatement(BlockStatement body, BlockStatement recover, String name, Region region) {
        super(region);
        this.body = body;
        this.recover = recover;
        this.name = name;
    }

    @Override
    public boolean isControlFlowReturned() {
        return body.isControlFlowReturned() && recover.isControlFlowReturned();
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        int regionStart = assembler.getOffset(assembler.getPosition());
        body.compile(context, assembler);
        int regionEnd = assembler.getOffset(assembler.getPosition());

        Assembler.Label end = body.isControlFlowReturned() ? null : assembler.jump(Operation.JUMP);

        int offset = assembler.getOffset(assembler.getPosition());
        int slot = -1;

        try (Context recoverContext = context.enter()) {
            if (name != null) {
                slot = recoverContext.addSlot(name);
            }
            recover.compile(recoverContext, assembler);
        }

        assembler.bind(end);

        FunctionContext functionContext = context.unwrap(FunctionContext.class);

        if (slot >= 0) {
            functionContext.addGuard(regionStart, regionEnd, offset, slot);
        } else {
            functionContext.addGuard(regionStart, regionEnd, offset);
        }
    }

    public BlockStatement getBody() {
        return body;
    }

    public BlockStatement getRecover() {
        return recover;
    }

    public String getName() {
        return name;
    }
}
