package com.shade.lang.parser.node.expr;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.compiler.Opcode;
import com.shade.lang.parser.ScriptException;
import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

import java.util.Objects;

public class LoadIndexExpression extends Expression {
    private final Expression owner;
    private final Expression index;

    public LoadIndexExpression(Expression owner, Expression index, Region region) {
        super(region);
        this.owner = owner;
        this.index = index;
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        owner.compile(context, assembler);
        index.compile(context, assembler);
        assembler.imm8(Opcode.GET_INDEX);
        assembler.addTraceLine(getRegion().getBegin());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoadIndexExpression that = (LoadIndexExpression) o;
        return owner.equals(that.owner) &&
            index.equals(that.index);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, index);
    }

    public Expression getOwner() {
        return owner;
    }

    public Expression getIndex() {
        return index;
    }
}
