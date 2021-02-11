package com.shade.lang.compiler.optimizer;

import com.shade.lang.util.annotations.NotNull;

public interface TransformerProvider {
    @NotNull
    Transformer create();

    int getLevel();
}
