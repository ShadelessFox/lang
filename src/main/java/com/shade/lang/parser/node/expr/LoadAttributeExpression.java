package com.shade.lang.parser.node.expr;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.compiler.Operand;
import com.shade.lang.compiler.Operation;
import com.shade.lang.parser.ScriptException;
import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

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
