package com.shade.lang.parser.node;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

public abstract class Node {
    private final Region region;

    public Node(Region region) {
        this.region = region;
    }

    public abstract void compile(Context context, Assembler assembler);

    public abstract void accept(Visitor visitor);

    public final Region getRegion() {
        return region;
    }
}
