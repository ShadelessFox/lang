package com.shade.lang.compiler.optimizer;

import com.shade.lang.compiler.parser.node.Node;
import com.shade.lang.compiler.parser.node.visitor.AbstractVisitor;
import com.shade.lang.util.annotations.NotNull;

public abstract class Transformer extends AbstractVisitor {
    @Override
    public boolean enterDefault(@NotNull Node node) {
        return true;
    }

    @NotNull
    @Override
    public <T extends Node> T leaveDefault(@NotNull T node) {
        return node;
    }
}
