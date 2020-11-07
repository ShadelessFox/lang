package com.shade.lang.parser.node.context;

import java.util.Iterator;

public class FunctionContext extends Context {
    public FunctionContext(Context parent) {
        super(parent);
        this.nextLocalSlot = 0;
    }

    @Override
    public Iterator<Scope> iterator() {
        return new Iterator<Scope>() {
            private int index = scopes.size() - 1;

            @Override
            public boolean hasNext() {
                return index >= 0;
            }

            @Override
            public Scope next() {
                return scopes.get(index--);
            }
        };
    }
}
