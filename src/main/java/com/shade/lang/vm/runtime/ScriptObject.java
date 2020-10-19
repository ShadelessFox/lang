package com.shade.lang.vm.runtime;

import java.util.Map;

public interface ScriptObject {
    Map<String, ScriptObject> getAttributes();
}
