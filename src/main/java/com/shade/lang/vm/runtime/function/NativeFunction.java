package com.shade.lang.vm.runtime.function;

import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.Module;
import com.shade.lang.vm.runtime.ScriptObject;
import com.shade.lang.vm.runtime.value.Value;

import java.util.function.BiFunction;

public class NativeFunction extends Function {
    private final BiFunction<Machine, ScriptObject[], Object> prototype;

    public NativeFunction(Module module, String name, int argumentsCount, ScriptObject[] boundArgumentsCount, int flags, BiFunction<Machine, ScriptObject[], Object> prototype) {
        super(module, name, argumentsCount, boundArgumentsCount, flags);
        this.prototype = prototype;
    }

    public NativeFunction(Module module, String name, int argumentsCount, int flags, BiFunction<Machine, ScriptObject[], Object> prototype) {
        this(module, name, argumentsCount, null, flags, prototype);
    }

    @Override
    public void invoke(Machine machine, int argc) {
        ScriptObject[] arguments = prepare(machine, argc);

        if (arguments == null) {
            return;
        }

        machine.getCallStack().push(new Machine.Frame(this, null, null, null));

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
