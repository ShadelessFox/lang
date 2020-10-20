package com.shade.lang.parser.node.expr;

import com.shade.lang.parser.node.Node;
import com.shade.lang.parser.node.Visitor;

public interface Expression extends Node {
    void accept(Visitor visitor);
}
