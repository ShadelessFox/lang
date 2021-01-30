package com.shade.lang.compiler.parser.node.stmt;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.assembler.Operand;
import com.shade.lang.compiler.assembler.Operation;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Expression;
import com.shade.lang.compiler.parser.node.Statement;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.node.context.FinallyContext;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.runtime.objects.value.NoneValue;
import com.shade.lang.util.annotations.NotNull;
import com.shade.lang.util.annotations.Nullable;

import java.util.Objects;

public class ReturnStatement extends Statement {
    private final Expression value;

    public ReturnStatement(@Nullable Expression value, @NotNull Region region) {
        super(region);
        this.value = value;
    }

    @Override
    public boolean isControlFlowReturned() {
        return true;
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        final FinallyContext finallyContext = context.unwrap(FinallyContext.class);
        if (finallyContext != null) {
            finallyContext.compile(assembler);
        }

        if (value != null) {
            value.compile(context, assembler);
        } else {
            assembler.emit(Operation.PUSH, Operand.constant(NoneValue.INSTANCE));
        }

        assembler.emit(Operation.RETURN);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReturnStatement statement = (ReturnStatement) o;
        return Objects.equals(value, statement.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public Expression getValue() {
        return value;
    }
}
