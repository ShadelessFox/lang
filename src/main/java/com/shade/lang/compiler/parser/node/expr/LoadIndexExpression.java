package com.shade.lang.compiler.parser.node.expr;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.assembler.Operation;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Expression;
import com.shade.lang.compiler.parser.node.visitor.Visitor;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.util.annotations.NotNull;

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

    @NotNull
    @Override
    public Expression accept(@NotNull Visitor visitor) {
        if (visitor.enterLoadIndexExpression(this)) {
            final Expression owner = this.owner.accept(visitor);
            final Expression index = this.index.accept(visitor);

            if (owner != this.owner || index != this.index) {
                return visitor.leaveLoadIndexExpression(new LoadIndexExpression(owner, index, getRegion()));
            } else {
                return visitor.leaveLoadIndexExpression(this);
            }
        }

        return this;
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
