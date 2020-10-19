package com.shade.lang.vm.runtime.function;

import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.Module;

import java.util.stream.IntStream;
import java.util.function.Function;

public class NativeFunction extends AbstractFunction {
    private final Function<Object[], Object> prototype;

    public NativeFunction(Module module, String name, Function<Object[], Object> prototype) {
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
        final Object[] arguments = IntStream.range(0, argc).mapToObj(x -> machine.getOperandStack().pop()).toArray();
        final Object result = prototype.apply(arguments);
        machine.getCallStack().pop();
        machine.getOperandStack().push(result);
    }
}
