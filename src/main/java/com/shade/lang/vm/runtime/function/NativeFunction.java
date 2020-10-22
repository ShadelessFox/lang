package com.shade.lang.vm.runtime.function;

import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.Module;
import com.shade.lang.vm.runtime.ScriptObject;

import java.util.function.Function;

public class NativeFunction extends AbstractFunction {
    private final Function<ScriptObject[], ScriptObject> prototype;

    public NativeFunction(Module module, String name, Function<ScriptObject[], ScriptObject> prototype) {
        super(module, name);
        this.prototype = prototype;
    }

    @Override
    public void invoke(Machine machine, int argc) {
        /*
         * Push fake frame so we can have a proper
         * stack trace if interpreter will halt inside
         * native function call.
         */

        ScriptObject[] locals = new ScriptObject[argc];
        for (int index = argc; index > 0; index--) {
            locals[index - 1] = machine.getOperandStack().pop();
        }

        machine.getCallStack().push(new Machine.NativeFrame(this));
        machine.getOperandStack().push(prototype.apply(locals));
        machine.getCallStack().pop();
    }
}
