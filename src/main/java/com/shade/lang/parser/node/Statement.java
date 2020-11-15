package com.shade.lang.parser.node;

import com.shade.lang.optimizer.Transformer;
import com.shade.lang.parser.node.visitor.Visitor;
import com.shade.lang.parser.token.Region;

public abstract class Statement extends Node {
    public Statement(Region region) {
        super(region);
    }

    public boolean isControlFlowReturned() {
        return false;
    }

    public boolean isControlFlowInterrupted() {
        return false;
    }

    @Override
    public final void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Statement transform(Transformer transformer) {
        return transformer.transform(this);
    }
}
