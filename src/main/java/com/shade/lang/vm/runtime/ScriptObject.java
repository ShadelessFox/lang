package com.shade.lang.vm.runtime;

import java.util.HashMap;
import java.util.Map;

public abstract class ScriptObject {
    protected final Map<String, ScriptObject> attributes;
    protected final boolean immutable;

    public ScriptObject(boolean immutable) {
        this.attributes = new HashMap<>();
        this.immutable = immutable;
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

    public boolean isImmutable() {
        return immutable;
    }
}
