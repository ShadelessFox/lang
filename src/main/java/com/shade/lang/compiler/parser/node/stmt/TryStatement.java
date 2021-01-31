package com.shade.lang.compiler.parser.node.stmt;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.assembler.Operand;
import com.shade.lang.compiler.assembler.Operation;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Statement;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.node.context.FinallyContext;
import com.shade.lang.compiler.parser.node.context.FunctionContext;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.runtime.objects.value.NoneValue;
import com.shade.lang.util.annotations.NotNull;
import com.shade.lang.util.annotations.Nullable;

import java.util.UUID;

public class TryStatement extends Statement {
    private final BlockStatement body;
    private final BlockStatement recoverBody;
    private final BlockStatement finallyBody;
    private final String name;

    public TryStatement(@NotNull BlockStatement body, @Nullable BlockStatement recoverBody, @Nullable String name, @Nullable BlockStatement finallyBody, @NotNull Region region) {
        super(region);
        this.body = body;
        this.recoverBody = recoverBody;
        this.finallyBody = finallyBody;
        this.name = name;
    }

    @Override
    public boolean isControlFlowReturned() {
        return body.isControlFlowReturned() &&
            (recoverBody == null || recoverBody.isControlFlowReturned()) &&
            (finallyBody == null || finallyBody.isControlFlowReturned());
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        final FinallyContext finallyContext = new FinallyContext(context, this);

        int regionStart = assembler.getOffset(assembler.getPosition());
        body.compile(finallyContext, assembler);
        int regionEnd = assembler.getOffset(assembler.getPosition());

        finallyContext.compile(assembler);

        if (recoverBody != null) {
            Assembler.Label end = body.isControlFlowReturned() ? null : assembler.jump(Operation.JUMP);

            int offset = assembler.getOffset(assembler.getPosition());
            int slot = Operand.UNDEFINED;

            try (Context recoverContext = finallyContext.enter()) {
                if (name != null) {
                    slot = recoverContext.addSlot(name);
                }
                recoverBody.compile(recoverContext, assembler);

                finallyContext.compile(assembler);
            }

            assembler.bind(end);

            FunctionContext functionContext = context.unwrap(FunctionContext.class);
            functionContext.addGuard(regionStart, regionEnd, offset, slot);
        }

        if (finallyBody != null) {
            regionEnd = assembler.getOffset(assembler.getPosition());

            Assembler.Label end = body.isControlFlowReturned() ? null : assembler.jump(Operation.JUMP);

            int offset = assembler.getOffset(assembler.getPosition());
            int slot = context.addSlot(UUID.randomUUID().toString());

            finallyContext.compile(assembler);
            assembler.emit(Operation.GET_LOCAL, Operand.imm8(slot));
            assembler.emit(Operation.PUSH, Operand.constant(NoneValue.INSTANCE));
            assembler.emit(Operation.CMP_EQ);
            Assembler.Label throwEnd = assembler.jump(Operation.JUMP_IF_TRUE);
            assembler.emit(Operation.GET_LOCAL, Operand.imm8(slot));
            assembler.emit(Operation.THROW);

            assembler.bind(throwEnd);
            assembler.bind(end);

            FunctionContext functionContext = context.unwrap(FunctionContext.class);
            functionContext.addGuard(regionStart, regionEnd, offset, slot);
        }
    }

    @NotNull
    public BlockStatement getBody() {
        return body;
    }

    @Nullable
    public BlockStatement getRecoverBody() {
        return recoverBody;
    }

    @Nullable
    public BlockStatement getFinallyBody() {
        return finallyBody;
    }

    @Nullable
    public String getName() {
        return name;
    }
}
