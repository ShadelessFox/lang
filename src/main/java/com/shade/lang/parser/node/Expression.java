package com.shade.lang.parser.node;

import com.shade.lang.optimizer.Transformer;
import com.shade.lang.parser.token.Region;

public abstract class Expression extends Node {
    public Expression(Region region) {
        super(region);
    }

    @Override
    public Expression transform(Transformer transformer) {
        return transformer.transform(this);
    }
}
