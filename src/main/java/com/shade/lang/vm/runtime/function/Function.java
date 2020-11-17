package com.shade.lang.vm.runtime.function;

import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.Module;
import com.shade.lang.vm.runtime.ScriptObject;

public abstract class Function extends ScriptObject {
    public static final int FLAG_VARIADIC = 1;

    protected final Module module;
    protected final String name;
    protected final int argumentsCount;
    protected final byte flags;

    public Function(Module module, String name, int argumentsCount, int flags) {
        super(true);
        this.module = module;
        this.name = name;
        this.argumentsCount = argumentsCount;
        this.flags = (byte) (flags & 0xff);
    }

    public abstract void invoke(Machine machine, int argc);

    protected boolean isInvalidArguments(Machine machine, int passedArgumentsCount) {
        boolean variadic = hasFlag(Function.FLAG_VARIADIC);

        if ((variadic && argumentsCount - 1 <= passedArgumentsCount)) {
            return false;
        }

        if (!variadic && argumentsCount == passedArgumentsCount) {
            return false;
        }

        machine.panic(
            String.format(
                "Function '%s' takes %d%s %s but %d %s provided",
                getName(),
                argumentsCount,
                variadic ? " or more" : "",
                argumentsCount != 1 || variadic ? "arguments" : "argument",
                passedArgumentsCount,
                passedArgumentsCount != 1 ? "were" : "was"
            ),
            true
        );

        return true;
    }

    public Module getModule() {
        return module;
    }

    public String getName() {
        return name;
    }

    public int getArgumentsCount() {
        return argumentsCount;
    }

    public byte getFlags() {
        return flags;
    }

    public boolean hasFlag(int bit) {
        return (flags & bit) == 1;
    }
}
