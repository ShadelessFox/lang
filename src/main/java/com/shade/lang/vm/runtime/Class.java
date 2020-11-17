package com.shade.lang.vm.runtime;

import com.shade.lang.vm.runtime.function.NativeFunction;
import com.shade.lang.vm.runtime.function.RuntimeFunction;

import java.util.Map;

public class Class extends ScriptObject {
    private final String name;
    private final Class[] bases;

    public Class(String name, Class[] bases) {
        super(true);
        this.name = name;
        this.bases = bases;
        populateInheritedAttributes(this);
    }

    public Instance instantiate() {
        Instance instance = new Instance(this);

        for (Map.Entry<String, ScriptObject> attribute : getAttributes().entrySet()) {
            if (attribute.getValue() instanceof RuntimeFunction) {
                RuntimeFunction function = (RuntimeFunction) attribute.getValue();

                /*
                 * Don't bind self parameter for constructors because
                 * they can be called with derived class' instance
                 */
                if (function.getName().equals("<init>")) {
                    instance.setAttribute(attribute.getKey(), function);
                    continue;
                }

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
            } else if (attribute.getValue() instanceof NativeFunction) {
                NativeFunction function = (NativeFunction) attribute.getValue();

                /*
                 * Don't bind self parameter for constructors because
                 * they can be called with derived class' instance
                 */
                if (function.getName().equals("<init>")) {
                    instance.setAttribute(attribute.getKey(), function);
                    continue;
                }

                NativeFunction boundFunction = new NativeFunction(
                    function.getModule(),
                    function.getName(),
                    function.getArgumentsCount(),
                    function.getFlags(),
                    function.getPrototype()
                );
                boundFunction.addBoundArgument(instance);

                instance.setAttribute(attribute.getKey(), boundFunction);
            }
        }

        return instance;
    }

    private void populateInheritedAttributes(Class child) {
        for (Class base : bases) {
            base.populateInheritedAttributes(child);
        }

        if (child != this) {
            child.getAttributes().putAll(getAttributes());
        }
    }

    public String getName() {
        return name;
    }

    public Class[] getBases() {
        return bases;
    }

    @Override
    public String toString() {
        return "[Class " + name + "]";
    }
}
