package com.shade.lang.parser.node.stmt;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.node.Visitor;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.node.expr.Expression;
import com.shade.lang.parser.token.Region;

public class DeclareVariableStatement implements Statement {
    private final String name;
    private final Expression value;
    private final Region region;

    public DeclareVariableStatement(String name, Expression value, Region region) {
        this.name = name;
        this.value = value;
        this.region = region;
    }

    public String getName() {
        return name;
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public boolean isControlFlowReturned() {
        return false;
    }

    @Override
    public Region getRegion() {
        return region;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void emit(Context context, Assembler assembler) {
        throw new RuntimeException("Not implemented");
    }
}
