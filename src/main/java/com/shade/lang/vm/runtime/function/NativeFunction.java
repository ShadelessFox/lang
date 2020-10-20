package com.shade.lang.vm.runtime.function;

import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.Module;
import com.shade.lang.vm.runtime.ScriptObject;

import java.util.stream.IntStream;
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

        machine.getCallStack().push(new Machine.NativeFrame(this));
        final ScriptObject[] arguments = IntStream.range(0, argc).mapToObj(x -> machine.getOperandStack().pop()).toArray(ScriptObject[]::new);
        final ScriptObject result = prototype.apply(arguments);
        machine.getCallStack().pop();
        machine.getOperandStack().push(result);
    }
}
