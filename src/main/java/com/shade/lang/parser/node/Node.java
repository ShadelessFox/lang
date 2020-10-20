package com.shade.lang.parser.node;

public interface Node extends Generator {
    void accept(Visitor visitor);
}
