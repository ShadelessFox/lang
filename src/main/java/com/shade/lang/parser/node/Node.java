package com.shade.lang.parser.node;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.optimizer.Transformer;
import com.shade.lang.parser.ScriptException;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.node.visitor.Visitor;
import com.shade.lang.parser.token.Region;

public abstract class Node {
    private final Region region;

    public Node(Region region) {
        this.region = region;
    }

    public abstract void compile(Context context, Assembler assembler) throws ScriptException;

    public abstract void accept(Visitor visitor);

    public abstract Node transform(Transformer transformer);

    public final Region getRegion() {
        return region;
    }
}
