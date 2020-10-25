package com.shade.lang.vm.runtime.function;

import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.Module;
import com.shade.lang.vm.runtime.ScriptObject;

import java.util.function.BiFunction;

public class NativeFunction extends Function {
    private final BiFunction<Machine, ScriptObject[], ScriptObject> prototype;

    public NativeFunction(Module module, String name, BiFunction<Machine, ScriptObject[], ScriptObject> prototype) {
        super(module, name);
        this.prototype = prototype;
    }

    @Override
    public void invoke(Machine machine, int argc) {
        ScriptObject[] locals = new ScriptObject[argc];
        for (int index = argc; index > 0; index--) {
            locals[index - 1] = machine.getOperandStack().pop();
        }

        /*
         * Push fake frame so we can have a proper
         * stack trace if interpreter will halt inside
         * native function call.
         */
        machine.getCallStack().push(new Machine.NativeFrame(this));

        ScriptObject result = prototype.apply(machine, locals);

        if (result != null) {
            machine.getOperandStack().push(result);
            machine.getCallStack().pop();
        }
    }
}
