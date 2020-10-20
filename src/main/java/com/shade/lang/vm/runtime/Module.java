package com.shade.lang.vm.runtime;

public class Module extends ScriptObject {
    private final String name;
    private final String source;

    public Module(String name, String source) {
        this.name = name;
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public String getSource() {
        return source;
    }
}
