package com.shade.lang.parser.node.stmt;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.compiler.Opcode;
import com.shade.lang.parser.ScriptException;
import com.shade.lang.parser.node.Statement;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

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
        int regionStart = assembler.getPosition();
        body.compile(context, assembler);
        int regionEnd = assembler.getPosition();
        Assembler.Label end = body.isControlFlowReturned() ? null : assembler.jump(Opcode.JUMP);
        int offset = assembler.getPosition();
        Context recoverContext = context.wrap();
        int slot = name == null ? -1 : recoverContext.addSlot(name);
        recover.compile(recoverContext, assembler);
        assembler.bind(end);
        assembler.addGuard(regionStart, regionEnd, offset, slot);
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
