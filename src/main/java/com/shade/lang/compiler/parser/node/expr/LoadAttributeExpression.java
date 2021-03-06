package com.shade.lang.compiler.parser.node.expr;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.assembler.Operand;
import com.shade.lang.compiler.assembler.Operation;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Expression;
import com.shade.lang.compiler.parser.node.visitor.Visitor;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.util.annotations.NotNull;

import java.util.Objects;

public class LoadAttributeExpression extends Expression {
    private final Expression owner;
    private final String name;

    public LoadAttributeExpression(Expression owner, String name, Region region) {
        super(region);
        this.owner = owner;
        this.name = name;
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        owner.compile(context, assembler);
        assembler.emit(Operation.GET_ATTRIBUTE, Operand.constant(name));
        assembler.addLocation(getRegion().getBegin());
    }

    @NotNull
    @Override
    public Expression accept(@NotNull Visitor visitor) {
        if (visitor.enterLoadAttributeExpression(this)) {
            final Expression owner = this.owner.accept(visitor);

            if (owner != this.owner) {
                return visitor.leaveLoadAttributeExpression(new LoadAttributeExpression(owner, name, getRegion()));
            } else {
                return visitor.leaveLoadAttributeExpression(this);
            }
        }

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoadAttributeExpression that = (LoadAttributeExpression) o;
        return owner.equals(that.owner) &&
            name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, name);
    }

    public Expression getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }
}
