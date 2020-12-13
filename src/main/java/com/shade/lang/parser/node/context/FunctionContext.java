package com.shade.lang.parser.node.context;

import com.shade.lang.util.annotations.NotNull;
import com.shade.lang.vm.runtime.function.Guard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class FunctionContext extends Context {
    private final List<Guard> guards;

    public FunctionContext(Context parent) {
        super(parent);
        this.nextLocalSlot = 0;
        this.guards = new ArrayList<>();
    }

    public List<Guard> getGuards() {
        return Collections.unmodifiableList(guards);
    }

    public void addGuard(int start, int end, int offset, int slot) {
        guards.add(new Guard(start, end, offset, slot));
    }

    public void addGuard(int start, int end, int offset) {
        guards.add(new Guard(start, end, offset, -1));
    }

    @NotNull
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
