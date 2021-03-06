package com.shade.lang.compiler.parser.node.context;

import com.shade.lang.runtime.objects.function.Guard;
import com.shade.lang.util.annotations.NotNull;

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
