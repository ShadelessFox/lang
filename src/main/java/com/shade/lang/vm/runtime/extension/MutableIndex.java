package com.shade.lang.vm.runtime.extension;

import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.ScriptObject;

public interface MutableIndex {
    void setIndex(Machine machine, ScriptObject index, ScriptObject value);
}
