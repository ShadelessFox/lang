package com.shade.lang.parser.node;

public interface Node {
    void accept(Visitor visitor);
}
