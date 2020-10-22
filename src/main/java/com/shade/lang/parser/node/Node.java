package com.shade.lang.parser.node;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

public interface Node {
    Region getRegion();

    void emit(Context context, Assembler assembler);

    void accept(Visitor visitor);
}
