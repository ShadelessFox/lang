package com.shade.lang.compiler.parser.node;

import com.shade.lang.compiler.optimizer.Transformer;
import com.shade.lang.compiler.parser.node.visitor.Visitor;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.util.annotations.NotNull;

public abstract class Expression extends Node {
    public Expression(@NotNull Region region) {
        super(region);
    }

    @Override
    @Deprecated
    public Expression transform(Transformer transformer) {
        return transformer.transform(this);
    }

    @NotNull
    @Override
    public abstract Expression accept(@NotNull Visitor visitor);
}
