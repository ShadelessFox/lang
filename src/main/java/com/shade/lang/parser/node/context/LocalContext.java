package com.shade.lang.parser.node.context;

import com.shade.lang.vm.runtime.Module;

import java.util.ArrayList;
import java.util.List;

public class LocalContext extends Context {
    private final List<String> slots = new ArrayList<>();

    public LocalContext(Context parent) {
        super(parent);
    }

    public LocalContext(Module module) {
        super(module);
    }

    public boolean hasSlot(String name) {
        return slots.contains(name);
    }

    public byte getSlot(String name) {
        if (!slots.contains(name)) {
            slots.add(name);
        }
        if (slots.size() > 255) {
            throw new RuntimeException("Too many locals (allowed 255 maximum)");
        }
        return (byte) slots.indexOf(name);
    }

    public List<String> getSlots() {
        return slots;
    }
}
