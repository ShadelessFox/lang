package com.shade.lang.parser.node.expr;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.compiler.Operand;
import com.shade.lang.compiler.Operation;
import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

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
