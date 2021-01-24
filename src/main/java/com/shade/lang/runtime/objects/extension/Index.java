package com.shade.lang.runtime.objects.extension;

import com.shade.lang.runtime.Machine;
import com.shade.lang.runtime.objects.ScriptObject;

public interface Index {
    ScriptObject getIndex(Machine machine, ScriptObject index);
}
