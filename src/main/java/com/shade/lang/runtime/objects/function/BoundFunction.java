package com.shade.lang.runtime.objects.function;

import com.shade.lang.runtime.Machine;
import com.shade.lang.runtime.objects.ScriptObject;
import com.shade.lang.util.annotations.NotNull;
import com.shade.lang.util.annotations.Nullable;

public class BoundFunction extends Function {
    private final Function function;
    private final ScriptObject[] boundArguments;

    public BoundFunction(@NotNull Function function, @NotNull ScriptObject[] boundArguments) {
        super(function.module, function.name, function.arity, function.flags);
        this.function = function;
        this.boundArguments = boundArguments;
    }

    @Nullable
    @Override
    protected ScriptObject[] prepare(@NotNull Machine machine, int argc) {
        final ScriptObject[] locals = super.prepare(machine, argc);

        if (locals == null) {
            return null;
        }

        final ScriptObject[] boundLocals = new ScriptObject[argc + boundArguments.length];
        System.arraycopy(boundArguments, 0, boundLocals, 0, boundArguments.length);
        System.arraycopy(locals, 0, boundLocals, boundArguments.length, locals.length);

        return boundLocals;
    }

    @Override
    protected void invoke(@NotNull Machine machine, @NotNull ScriptObject[] arguments) {
        function.invoke(machine, arguments);
    }

    @NotNull
    public ScriptObject[] getBoundArguments() {
        return boundArguments;
    }

    @Override
    public String toString() {
        return "[Bound Function '" + name + "']";
    }
}
