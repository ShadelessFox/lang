package com.shade.lang.compiler.parser.node;

import com.shade.lang.compiler.optimizer.Transformer;
import com.shade.lang.compiler.parser.token.Region;

public abstract class Expression extends Node {
    public Expression(Region region) {
        super(region);
    }

    @Override
    public Expression transform(Transformer transformer) {
        return transformer.transform(this);
    }
}
