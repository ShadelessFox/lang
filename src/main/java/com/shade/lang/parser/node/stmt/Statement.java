package com.shade.lang.parser.node.stmt;

import com.shade.lang.parser.node.Emittable;
import com.shade.lang.parser.node.Node;

public interface Statement extends Node, Emittable {
    boolean isControlFlowReturned();
}
