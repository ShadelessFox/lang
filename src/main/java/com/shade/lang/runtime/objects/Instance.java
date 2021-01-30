package com.shade.lang.runtime.objects;

import com.shade.lang.runtime.objects.function.BoundFunction;
import com.shade.lang.runtime.objects.function.Function;
import com.shade.lang.util.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Instance extends ScriptObject {
    private final Class base;
    private final Map<Class, Map<String, ScriptObject>> instanceAttributes;

    public Instance(@NotNull Class base) {
        super(false);
        this.base = base;
        this.instanceAttributes = getInstanceAttributes0();
        this.attributes.putAll(getOwnInstanceAttributes());
    }

    @NotNull
    public Class getBase() {
        return base;
    }

    @NotNull
    public Map<Class, Map<String, ScriptObject>> getInstanceAttributes() {
        return instanceAttributes;
    }

    @NotNull
    private Map<Class, Map<String, ScriptObject>> getInstanceAttributes0() {
        final Map<Class, Map<String, ScriptObject>> instanceAttributes = new HashMap<>();
        final Class[] resolutionOrder = base.getResolutionOrder();

        for (int index = resolutionOrder.length - 1; index >= 0; index--) {
            final Class cls = resolutionOrder[index];
            final Map<String, ScriptObject> attributes = new LinkedHashMap<>();

            for (Map.Entry<String, ScriptObject> entry : cls.attributes.entrySet()) {
                attributes.put(entry.getKey(), getInstantiatedAttribute(entry.getValue()));
            }

            instanceAttributes.put(cls, attributes);
        }

        return instanceAttributes;
    }

    @NotNull
    private ScriptObject getInstantiatedAttribute(@NotNull ScriptObject attribute) {
        if (attribute instanceof Function) {
            final Function function = (Function) attribute;

            if (function instanceof BoundFunction) {
                throw new IllegalArgumentException("Attribute function already bound: " + attribute);
            }

            if (!function.getName().contains("<init>")) {
                // TODO: This is OK for now, but we can do better
                //       when static functions will be introduced.
                return new BoundFunction(function, (byte) (function.getArity() - 1), new ScriptObject[]{this});
            }
        }

        return attribute;
    }

    @NotNull
    public Map<String, ScriptObject> getOwnInstanceAttributes() {
        return instanceAttributes.get(base);
    }

    @Override
    public String toString() {
        return "[Object " + base.getName() + '@' + Integer.toHexString(hashCode()) + "]";
    }
}
