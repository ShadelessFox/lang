package com.shade.lang.compiler.parser.node.expr;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.assembler.Operand;
import com.shade.lang.compiler.assembler.Operation;
import com.shade.lang.compiler.parser.node.Expression;
import com.shade.lang.compiler.parser.node.visitor.Visitor;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.util.annotations.NotNull;

import java.util.Objects;

public class LoadSymbolExpression extends Expression {
    private final String name;

    public LoadSymbolExpression(String name, Region region) {
        super(region);
        this.name = name;
    }

    @Override
    public void compile(Context context, Assembler assembler) {
        if (context.hasSlot(name)) {
            assembler.emit(Operation.GET_LOCAL, Operand.imm8(context.addSlot(name)));
        } else {
            assembler.emit(Operation.GET_GLOBAL, Operand.constant(name));
        }

        assembler.addLocation(getRegion().getBegin());
    }

    @NotNull
    @Override
    public Expression accept(@NotNull Visitor visitor) {
        if (visitor.enterLoadSymbolExpression(this)) {
            return visitor.leaveLoadSymbolExpression(this);
        }

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoadSymbolExpression that = (LoadSymbolExpression) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public String getName() {
        return name;
    }
}
