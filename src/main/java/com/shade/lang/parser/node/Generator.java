package com.shade.lang.parser.node;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.vm.runtime.Module;

public interface Generator {
    void emit(Module module, Assembler assembler);
}
