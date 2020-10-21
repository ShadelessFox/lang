package com.shade.lang.parser.node;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.vm.runtime.Module;

public interface Node {
    void emit(Module module, Assembler assembler);

    void accept(Visitor visitor);
}
