package com.shade.lang.runtime.objects.function;

import com.shade.lang.runtime.Machine;
import com.shade.lang.runtime.frames.Frame;
import com.shade.lang.runtime.frames.NativeFrame;
import com.shade.lang.runtime.objects.ScriptObject;
import com.shade.lang.runtime.objects.module.Module;
import com.shade.lang.runtime.objects.value.Value;
import com.shade.lang.util.annotations.NotNull;
import com.shade.lang.util.annotations.Nullable;

public class NativeFunction extends Function {
    private final Prototype prototype;

    public NativeFunction(@NotNull Module module, @NotNull String name, byte arity, byte flags, @NotNull Prototype prototype) {
        super(module, name, arity, flags);
        this.prototype = prototype;
    }

    @Override
    public void invoke(@NotNull Machine machine, @NotNull ScriptObject[] arguments) {
        final Frame frame = new NativeFrame(module, this, machine.getOperandStack().size());
        machine.profilerBeginFrame(frame);
        machine.getCallStack().push(frame);

        Object result = prototype.invoke(machine, arguments);

        if (!(result instanceof Value)) {
            result = Value.from(result);
        }

        if (result != null) {
            machine.getOperandStack().push((ScriptObject) result);
            machine.getCallStack().pop();
        }
    }

    @NotNull
    public Prototype getPrototype() {
        return prototype;
    }

    @Override
    public String toString() {
        return "[Native Function '" + name + "']";
    }

    public interface Prototype {
        @Nullable
        Object invoke(@NotNull Machine machine, @NotNull ScriptObject... arguments);
    }
}
