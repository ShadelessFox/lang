package com.shade.lang.runtime.objects.function;

import com.shade.lang.runtime.Machine;
import com.shade.lang.runtime.frames.Frame;
import com.shade.lang.runtime.frames.NativeFrame;
import com.shade.lang.runtime.objects.module.Module;
import com.shade.lang.runtime.objects.ScriptObject;
import com.shade.lang.runtime.objects.value.Value;

import java.util.function.BiFunction;

public class NativeFunction extends Function {
    private final BiFunction<Machine, ScriptObject[], Object> prototype;

    public NativeFunction(Module module, String name, int argumentsCount, ScriptObject[] boundArguments, byte flags, BiFunction<Machine, ScriptObject[], Object> prototype) {
        super(module, name, argumentsCount, boundArguments, flags);
        this.prototype = prototype;
    }

    public NativeFunction(Module module, String name, int argumentsCount, byte flags, BiFunction<Machine, ScriptObject[], Object> prototype) {
        this(module, name, argumentsCount, null, flags, prototype);
    }

    @Override
    public void invoke(Machine machine, int argc) {
        ScriptObject[] arguments = prepare(machine, argc);

        if (arguments == null) {
            return;
        }

        Frame frame = new NativeFrame(module, this, machine.getOperandStack().size());
        machine.profilerBeginFrame(frame);
        machine.getCallStack().push(frame);

        Object result = prototype.apply(machine, arguments);

        if (!(result instanceof Value)) {
            result = Value.from(result);
        }

        if (result != null) {
            machine.getOperandStack().push((ScriptObject) result);
            machine.getCallStack().pop();
        }
    }

    public BiFunction<Machine, ScriptObject[], Object> getPrototype() {
        return prototype;
    }

    @Override
    public String toString() {
        return "[Native Function '" + getName() + "']";
    }
}
