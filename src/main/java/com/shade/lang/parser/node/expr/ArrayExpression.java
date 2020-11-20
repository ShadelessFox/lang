package com.shade.lang.parser.node.expr;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.parser.ScriptException;
import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

import java.util.List;

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

    public List<Expression> getElements() {
        return elements;
    }
}
