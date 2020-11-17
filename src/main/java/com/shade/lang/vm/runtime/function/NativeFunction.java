package com.shade.lang.vm.runtime.function;

import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.Module;
import com.shade.lang.vm.runtime.ScriptObject;
import com.shade.lang.vm.runtime.value.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class NativeFunction extends Function {
    private final BiFunction<Machine, Object[], Object> prototype;
    private final List<ScriptObject> boundArguments;

    public NativeFunction(Module module, String name, int argumentsCount, int flags, BiFunction<Machine, Object[], Object> prototype) {
        super(module, name, argumentsCount, flags);
        this.prototype = prototype;
        this.boundArguments = new ArrayList<>();
    }

    @Override
    public void invoke(Machine machine, int argc) {
        if (isInvalidArguments(machine, argc)) {
            return;
        }

        // TODO: Crazy (also partially shared) math here, refactor this please...

        boolean variadic = hasFlag(Function.FLAG_VARIADIC);

        Object[] arguments = new Object[boundArguments.size() + argumentsCount + (variadic ? 1 : 0)];

        if (variadic) {
            ScriptObject[] variadicArguments = new ScriptObject[argc - argumentsCount];
            for (int index = 0; index < variadicArguments.length; index++) {
                variadicArguments[variadicArguments.length - index - 1] = machine.getOperandStack().pop();
            }
            arguments[argumentsCount + boundArguments.size()] = variadicArguments;
        }

        for (int index = 0; index < argumentsCount; index++) {
            arguments[argumentsCount + boundArguments.size() - index - 1] = machine.getOperandStack().pop();
        }

        for (int index = 0; index < boundArguments.size(); index++) {
            arguments[index] = boundArguments.get(index);
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

    public BiFunction<Machine, Object[], Object> getPrototype() {
        return prototype;
    }

    public void addBoundArgument(ScriptObject object) {
        boundArguments.add(object);
    }

    @Override
    public String toString() {
        return "[Native Function '" + getName() + "']";
    }
}
