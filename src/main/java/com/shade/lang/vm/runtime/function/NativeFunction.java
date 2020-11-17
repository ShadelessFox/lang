package com.shade.lang.vm.runtime.function;

import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.Module;
import com.shade.lang.vm.runtime.ScriptObject;
import com.shade.lang.vm.runtime.value.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class NativeFunction extends Function {
    private final BiFunction<Machine, ScriptObject[], Object> prototype;
    private final List<ScriptObject> boundArguments;
    private final int argumentsCount;

    public NativeFunction(Module module, String name, int argumentsCount, int flags, BiFunction<Machine, ScriptObject[], Object> prototype) {
        super(module, name, flags);
        this.prototype = prototype;
        this.boundArguments = new ArrayList<>();
        this.argumentsCount = argumentsCount;
    }

    @Override
    public void invoke(Machine machine, int argc) {
        if (argc < argumentsCount || argumentsCount < argc && !hasFlag(Function.FLAG_VARIADIC)) {
            machine.panic("Native function '" + getName() + "' accepts " + argumentsCount + " argument" + (argumentsCount == 1 ? "" : "s") + " but " + argc + " " + (argc > 1 ? "were" : "was") + " provided", true);
        }
        ScriptObject[] locals = new ScriptObject[boundArguments.size() + argc];
        for (int index = 0; index < boundArguments.size(); index++) {
            locals[index] = boundArguments.get(index);
        }
        for (int index = argc; index > 0; index--) {
            locals[boundArguments.size() + index - 1] = machine.getOperandStack().pop();
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

    public BiFunction<Machine, ScriptObject[], Object> getPrototype() {
        return prototype;
    }

    public void addBoundArgument(ScriptObject object) {
        boundArguments.add(object);
    }

    public int getArgumentsCount() {
        return argumentsCount;
    }

    @Override
    public String toString() {
        return "[Native Function '" + getName() + "']";
    }
}
