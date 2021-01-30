package com.shade.lang.runtime.objects;

import com.shade.lang.util.annotations.NotNull;

public class Proxy extends ScriptObject {
    private final Instance instance;

    public Proxy(@NotNull Instance instance, Class base) {
        super(false);
        this.instance = instance;
        this.attributes.putAll(instance.getAttributes());
        this.attributes.putAll(instance.getInstanceAttributes().get(base));
    }

    @Override
    public void setAttribute(String name, ScriptObject value) {
        instance.setAttribute(name, value);
        attributes.put(name, value);
    }

    public Instance getInstance() {
        return instance;
    }
}
