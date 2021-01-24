package com.shade.lang.compiler.parser.node.expr;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.assembler.Operation;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Expression;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.token.Region;

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
        assembler.emit(Operation.GET_INDEX);
        assembler.addLocation(getRegion().getBegin());
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
