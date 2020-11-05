package com.shade.lang.parser.node.context;

import com.shade.lang.vm.runtime.Class;
import com.shade.lang.vm.runtime.ScriptObject;

import java.util.Map;

public class ClassContext extends Context {
    private final Class associatedClass;

    public ClassContext(Context parent, Class associatedClass) {
        super(parent);
        this.associatedClass = associatedClass;
    }

    @Override
    public void setAttribute(String name, ScriptObject value) {
        associatedClass.setAttribute(name, value);
    }

    @Override
    public ScriptObject getAttribute(String name) {
        return associatedClass.getAttribute(name);
    }

    @Override
    public Map<String, ScriptObject> getAttributes() {
        return associatedClass.getAttributes();
    }

    public Class getAssociatedClass() {
        return associatedClass;
    }
}
