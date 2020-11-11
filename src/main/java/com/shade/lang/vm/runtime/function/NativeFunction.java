package com.shade.lang.vm.runtime.function;

import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.Module;
import com.shade.lang.vm.runtime.ScriptObject;
import com.shade.lang.vm.runtime.value.Value;

import java.util.function.BiFunction;

public class NativeFunction extends Function {
    private final BiFunction<Machine, ScriptObject[], Object> prototype;

    public NativeFunction(Module module, String name, BiFunction<Machine, ScriptObject[], Object> prototype) {
        super(module, name);
        this.prototype = prototype;
    }

    @Override
    public void invoke(Machine machine, int argc) {
        ScriptObject[] locals = new ScriptObject[argc];
        for (int index = argc; index > 0; index--) {
            locals[index - 1] = machine.getOperandStack().pop();
        }

        machine.getCallStack().push(new Machine.Frame(this, null, null, null));

        Object result = prototype.apply(machine, locals);

        if (!(result instanceof Value)) {
            result = Value.from(result);
        }

        if (result != null) {
            machine.getOperandStack().push((ScriptObject) result);
            machine.getCallStack().pop();
        }
    }
}
