package com.shade.lang.compiler.parser.node.stmt;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.assembler.Operation;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Expression;
import com.shade.lang.compiler.parser.node.Statement;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.token.Region;

import java.util.Objects;

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
        value.compile(context, assembler);
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
