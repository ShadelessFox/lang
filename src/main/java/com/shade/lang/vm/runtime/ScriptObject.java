package com.shade.lang.vm.runtime;

import java.util.HashMap;
import java.util.Map;

public abstract class ScriptObject {
    protected final Map<String, ScriptObject> attributes;

    public ScriptObject(Map<String, ScriptObject> attributes) {
        this.attributes = attributes;
    }

    public ScriptObject() {
        this.attributes = new HashMap<>();
    }

    public void setAttribute(String name, ScriptObject value) {
        attributes.put(name, value);
    }

    public ScriptObject getAttribute(String name) {
        return attributes.get(name);
    }

    public Map<String, ScriptObject> getAttributes() {
        return attributes;
    }
}
