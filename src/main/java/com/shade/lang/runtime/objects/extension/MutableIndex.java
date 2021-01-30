package com.shade.lang.runtime.objects.extension;

import com.shade.lang.runtime.Machine;
import com.shade.lang.runtime.objects.ScriptObject;

public interface MutableIndex {
    void setIndex(Machine machine, ScriptObject index, ScriptObject value);
}
