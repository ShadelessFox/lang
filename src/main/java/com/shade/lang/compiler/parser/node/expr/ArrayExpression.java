package com.shade.lang.compiler.parser.node.expr;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Expression;
import com.shade.lang.compiler.parser.node.visitor.Visitor;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.util.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class ArrayExpression extends Expression {
    private final List<Expression> elements;

    public ArrayExpression(List<Expression> elements, Region region) {
        super(region);
        this.elements = elements;
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        Expression call = new CallExpression(
            new LoadAttributeExpression(
                new LoadSymbolExpression("<builtin>", getRegion()),
                "to_array",
                getRegion()
            ),
            elements,
            getRegion()
        );

        call.compile(context, assembler);
    }

    @NotNull
    @Override
    public Expression accept(@NotNull Visitor visitor) {
        if (visitor.enterArrayExpression(this)) {
            final List<Expression> elements = this.elements
                .stream()
                .map(x -> x.accept(visitor))
                .collect(Collectors.toList());

            if (!elements.equals(this.elements)) {
                return visitor.leaveArrayExpression(new ArrayExpression(elements, getRegion()));
            } else {
                return visitor.leaveArrayExpression(this);
            }
        }

        return this;
    }

    @NotNull
    public List<Expression> getElements() {
        return elements;
    }
}
