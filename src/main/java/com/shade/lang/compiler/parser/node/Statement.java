package com.shade.lang.compiler.parser.node;

import com.shade.lang.compiler.optimizer.Transformer;
import com.shade.lang.compiler.parser.token.Region;

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
    public Statement transform(Transformer transformer) {
        return transformer.transform(this);
    }
}