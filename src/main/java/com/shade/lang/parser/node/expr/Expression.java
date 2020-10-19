package com.shade.lang.parser.node.expr;

import com.shade.lang.parser.node.Emittable;
import com.shade.lang.parser.node.Node;
import com.shade.lang.parser.node.Visitor;

public interface Expression extends Node, Emittable {
    void accept(Visitor visitor);
}
