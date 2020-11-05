package com.shade.lang.vm.runtime;

public class Instance extends ScriptObject {
    private final Class base;

    public Instance(Class base) {
        this.base = base;
    }

    public Class getBase() {
        return base;
    }

    @Override
    public String toString() {
        return "[Object " + base.getName() + '@' + Integer.toHexString(hashCode()) + "]";
    }
}
