package com.shade.lang.vm.runtime.extension;

import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.ScriptObject;

public interface Index {
    ScriptObject getIndex(Machine machine, ScriptObject index);
}
