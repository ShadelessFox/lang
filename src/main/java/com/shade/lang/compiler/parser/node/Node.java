package com.shade.lang.compiler.parser.node;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.optimizer.Transformer;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.node.visitor.Visitor;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.util.annotations.NotNull;

public abstract class Node {
    private final Region region;

    public Node(Region region) {
        this.region = region;
    }

    public abstract void compile(Context context, Assembler assembler) throws ScriptException;

    public abstract Node transform(Transformer transformer);

    @NotNull
    public abstract Node accept(@NotNull Visitor visitor);

    @NotNull
    public final Region getRegion() {
        return region;
    }
}
