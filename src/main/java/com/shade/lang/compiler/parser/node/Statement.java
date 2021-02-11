package com.shade.lang.compiler.parser.node;

import com.shade.lang.compiler.optimizer.Transformer;
import com.shade.lang.compiler.parser.node.visitor.Visitor;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.util.annotations.NotNull;

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
    @Deprecated
    public Statement transform(Transformer transformer) {
        return transformer.transform(this);
    }

    @NotNull
    @Override
    public abstract Statement accept(@NotNull Visitor visitor);
}
