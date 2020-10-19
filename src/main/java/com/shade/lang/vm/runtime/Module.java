package com.shade.lang.vm.runtime;

import java.util.HashMap;
import java.util.Map;

public class Module implements ScriptObject {
    private final String name;
    private final String source;
    private final Map<String, ScriptObject> attributes;

    public Module(String name, String source) {
        this.name = name;
        this.source = source;
        this.attributes = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public String getSource() {
        return source;
    }

    @Override
    public Map<String, ScriptObject> getAttributes() {
        return attributes;
    }
}
