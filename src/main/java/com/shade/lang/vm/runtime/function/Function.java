package com.shade.lang.vm.runtime.function;

import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.Module;
import com.shade.lang.vm.runtime.ScriptObject;

public abstract class Function extends ScriptObject {
    private final Module module;
    private final String name;

    public Function(Module module, String name) {
        super(true);
        this.module = module;
        this.name = name;
    }

    public abstract void invoke(Machine machine, int argc);

    public Module getModule() {
        return module;
    }

    public String getName() {
        return name;
    }
}
