package com.shade.lang.parser.node;

import com.shade.lang.parser.token.Region;

public abstract class Expression extends Node {
    public Expression(Region region) {
        super(region);
    }

    @Override
    public final void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
