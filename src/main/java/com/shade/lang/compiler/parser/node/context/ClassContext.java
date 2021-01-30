package com.shade.lang.compiler.parser.node.context;

import com.shade.lang.util.annotations.NotNull;

public class ClassContext extends Context {
    private final String name;

    public ClassContext(@NotNull Context parent, @NotNull String name) {
        super(parent);
        this.name = name;
    }

    @NotNull
    public String getName() {
        return name;
    }
}
