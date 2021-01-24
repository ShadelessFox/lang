package com.shade.lang.runtime.objects;

public class Instance extends ScriptObject {
    private final Class base;

    public Instance(Class base) {
        super(false);
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
