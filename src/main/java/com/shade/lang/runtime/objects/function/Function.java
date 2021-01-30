package com.shade.lang.runtime.objects.function;

import com.shade.lang.runtime.Machine;
import com.shade.lang.runtime.objects.Chunk;
import com.shade.lang.runtime.objects.ScriptObject;
import com.shade.lang.runtime.objects.module.Module;
import com.shade.lang.runtime.objects.value.Value;
import com.shade.lang.util.annotations.NotNull;
import com.shade.lang.util.annotations.Nullable;

import java.util.Stack;

public abstract class Function extends ScriptObject {
    protected final Module module;
    protected final String name;
    protected final byte arity;
    protected final byte flags;

    public Function(@NotNull Module module, @NotNull String name, byte arity, byte flags) {
        super(true);
        this.module = module;
        this.name = name;
        this.arity = arity;
        this.flags = flags;
    }

    public final void invoke(@NotNull Machine machine, int argc) {
        final ScriptObject[] arguments = prepare(machine, argc);

        if (arguments == null) {
            return;
        }

        invoke(machine, arguments);
    }

    protected abstract void invoke(@NotNull Machine machine, @NotNull ScriptObject[] arguments);

    @Nullable
    protected ScriptObject[] prepare(@NotNull Machine machine, int argc) {
        final boolean variadic = (flags & Chunk.FLAG_VARIADIC) != 0;

        if (!variadic && argc != arity) {
            machine.panic(String.format(
                "Function '%s' accepts exactly %d argument%s but %s %s provided",
                name, arity, arity != 1 ? "s" : "", argc, argc != 1 ? "were" : "was"
            ), true);

            return null;
        }

        if (variadic && arity - 1 > argc) {
            machine.panic(String.format(
                "Function '%s' accepts %d or more arguments but %s %s provided",
                name, arity - 1, argc, argc != 1 ? "were" : "was"
            ), true);

            return null;
        }

        final ScriptObject[] locals = new ScriptObject[arity];
        final Stack<ScriptObject> stack = machine.getOperandStack();

        if (variadic) {
            final ScriptObject[] variadicLocals = new ScriptObject[argc - arity + 1];

            for (int index = argc - arity; index >= 0; index--) {
                variadicLocals[index] = stack.pop();
            }

            locals[arity - 1] = Value.from(variadicLocals);

            for (int index = arity - 2; index >= 0; index--) {
                locals[index] = stack.pop();
            }
        } else {
            for (int index = arity - 1; index >= 0; index--) {
                locals[index] = stack.pop();
            }
        }

        return locals;
    }

    @NotNull
    public Module getModule() {
        return module;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public byte getArity() {
        return arity;
    }

    public byte getFlags() {
        return flags;
    }
}
