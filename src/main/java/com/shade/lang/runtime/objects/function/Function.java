package com.shade.lang.runtime.objects.function;

import com.shade.lang.runtime.Machine;
import com.shade.lang.runtime.objects.Chunk;
import com.shade.lang.runtime.objects.module.Module;
import com.shade.lang.runtime.objects.ScriptObject;
import com.shade.lang.runtime.objects.value.Value;

import java.util.Stack;

public abstract class Function extends ScriptObject {
    protected final Module module;
    protected final String name;
    protected final int argumentsCount;
    protected final ScriptObject[] boundArguments;
    protected final byte flags;

    public Function(Module module, String name, int argumentsCount, ScriptObject[] boundArguments, int flags) {
        super(true);
        this.module = module;
        this.name = name;
        this.argumentsCount = argumentsCount;
        this.boundArguments = boundArguments != null ? boundArguments : new ScriptObject[0];
        this.flags = (byte) (flags & 0xff);
    }

    public abstract void invoke(Machine machine, int argc);

    protected ScriptObject[] prepare(Machine machine, int argc) {
        boolean variadic = hasFlag(Chunk.FLAG_VARIADIC);

        if ((!variadic && argumentsCount != argc) || (variadic && argumentsCount - 1 > argc)) {
            machine.panic(String.format(
                "Function '%s' takes %d%s %s but %d %s provided",
                getName(),
                variadic ? argumentsCount - 1 : argumentsCount,
                variadic ? " or more" : "",
                argumentsCount != 1 || variadic ? "arguments" : "argument",
                argc,
                argc != 1 ? "were" : "was"), true);
            return null;
        }

        ScriptObject[] values = new ScriptObject[boundArguments.length + argumentsCount];
        Stack<ScriptObject> stack = machine.getOperandStack();

        if (variadic) {
            ScriptObject[] variadicValues = new ScriptObject[argc - argumentsCount + 1];
            for (int index = argc - argumentsCount; index >= 0; index--) {
                variadicValues[index] = stack.pop();
            }
            values[argumentsCount - 1 + boundArguments.length] = Value.from(variadicValues);
        }

        for (int index = argumentsCount - (variadic ? 2 : 1); index >= 0; index--) {
            values[boundArguments.length + index] = stack.pop();
        }

        System.arraycopy(boundArguments, 0, values, 0, boundArguments.length);

        return values;
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

    public ScriptObject[] getBoundArguments() {
        return boundArguments;
    }

    public byte getFlags() {
        return flags;
    }

    public boolean hasFlag(int bit) {
        return (flags & bit) == 1;
    }
}
