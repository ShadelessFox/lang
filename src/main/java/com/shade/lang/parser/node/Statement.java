package com.shade.lang.parser.node;

import com.shade.lang.parser.node.visitor.Visitor;
import com.shade.lang.parser.token.Region;

public abstract class Statement extends Node {
    public Statement(Region region) {
        super(region);
    }

    public abstract boolean isControlFlowReturned();

    @Override
    public final void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
