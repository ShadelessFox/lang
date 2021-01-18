package com.shade.lang.vm.runtime;

import com.shade.lang.util.annotations.NotNull;
import com.shade.lang.vm.runtime.function.Function;
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
                    getFunctionName(function),
                    function.getFlags(),
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
                    getFunctionName(function),
                    function.getArgumentsCount(),
                    new ScriptObject[]{instance},
                    function.getFlags(),
                    function.getPrototype()
                );

                instance.setAttribute(attribute.getKey(), boundFunction);
            }
        }

        return instance;
    }

    public boolean isDerivedFrom(@NotNull Class cls) {
        if (this == cls) {
            return true;
        }

        for (Class base : bases) {
            if (base.isDerivedFrom(cls)) {
                return true;
            }
        }

        return false;
    }

    public boolean isInstance(@NotNull Instance instance) {
        return instance.getBase().isDerivedFrom(this);
    }

    private String getFunctionName(Function function) {
        return name + "::" + function.getName();
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
