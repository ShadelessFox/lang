package com.shade.lang.vm.runtime.function;

import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.Module;
import com.shade.lang.vm.runtime.ScriptObject;

public abstract class Function extends ScriptObject {
    public static final int FLAG_VARIADIC = 1;

    private final Module module;
    private final String name;
    private final byte flags;

    public Function(Module module, String name, int flags) {
        super(true);
        this.module = module;
        this.name = name;
        this.flags = (byte) (flags & 0xff);
    }

    public abstract void invoke(Machine machine, int argc);

    public Module getModule() {
        return module;
    }

    public String getName() {
        return name;
    }

    public byte getFlags() {
        return flags;
    }

    public boolean hasFlag(int bit) {
        return (flags & bit) == 1;
    }
}
