package com.shade.lang.parser.node.context;

import com.shade.lang.vm.runtime.Module;

import java.util.ArrayList;
import java.util.List;

public class LocalContext extends Context {
    private final List<String> locals = new ArrayList<>();

    public LocalContext(Context parent) {
        super(parent);
    }

    public LocalContext(Module module) {
        super(module);
    }

    public boolean hasSlot(String name) {
        return locals.contains(name);
    }

    public byte getSlot(String name) {
        if (!locals.contains(name)) {
            locals.add(name);
        }
        if (locals.size() > 255) {
            throw new RuntimeException("Too many locals (allowed 255 maximum)");
        }
        return (byte) locals.indexOf(name);
    }

    public List<String> getLocals() {
        return locals;
    }
}
