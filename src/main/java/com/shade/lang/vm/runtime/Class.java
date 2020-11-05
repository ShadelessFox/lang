package com.shade.lang.vm.runtime;

import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.function.Function;
import com.shade.lang.vm.runtime.function.RuntimeFunction;

import java.util.Map;

public class Class extends Function {
    private final Class[] bases;

    public Class(Module module, String name, Class[] bases) {
        super(module, name);
        this.bases = bases;
        populateInheritedAttributes(this);
    }

    @Override
    public void invoke(Machine machine, int argc) {
        Instance instance = new Instance(this);

        for (Map.Entry<String, ScriptObject> attribute : getAttributes().entrySet()) {
            if (attribute.getValue() instanceof RuntimeFunction) {
                RuntimeFunction function = (RuntimeFunction) attribute.getValue();

                if (function.getArgumentsCount() > 0) {
                    RuntimeFunction boundFunction = new RuntimeFunction(
                            function.getModule(),
                            function.getName(),
                            function.getChunk(),
                            function.getConstants(),
                            function.getLines(),
                            function.getGuards(),
                            function.getArgumentsCount() - 1,
                            1,
                            function.getLocalsCount()
                    );

                    boundFunction.getBoundArguments()[0] = instance;
                    instance.setAttribute(attribute.getKey(), boundFunction);
                }
            }
        }

        machine.getOperandStack().push(instance);
    }

    private void populateInheritedAttributes(Class child) {
        for (Class base : bases) {
            base.populateInheritedAttributes(child);
        }

        child.getAttributes().putAll(getAttributes());
    }

    public Class[] getBases() {
        return bases;
    }

    @Override
    public String toString() {
        return "[Class " + getName() + "]";
    }
}
