package com.shade.lang.vm.runtime.function;

import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.Module;
import com.shade.lang.vm.runtime.ScriptObject;

import java.util.Collections;
import java.util.Map;

public abstract class AbstractFunction implements ScriptObject {
    private final Module module;
    private final String name;

    public AbstractFunction(Module module, String name) {
        this.module = module;
        this.name = name;
    }

    public Module getModule() {
        return module;
    }

    public String getName() {
        return name;
    }

    @Override
    public Map<String, ScriptObject> getAttributes() {
        return Collections.emptyMap();
    }

    public abstract void invoke(Machine machine, int argc);
}
