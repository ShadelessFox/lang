package com.shade.lang.parser.node.context;

import com.shade.lang.compiler.Assembler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FunctionContext extends Context {
    private final List<Assembler.Guard> guards;

    public FunctionContext(Context parent) {
        super(parent);
        this.nextLocalSlot = 0;
        this.guards = new ArrayList<>();
    }

    public Assembler.Guard[] getGuards() {
        return guards.toArray(new Assembler.Guard[0]);
    }

    public void addGuard(int start, int end, int offset, int slot) {
        guards.add(new Assembler.Guard(start, end, offset, slot));
    }

    public void addGuard(int start, int end, int offset) {
        guards.add(new Assembler.Guard(start, end, offset, -1));
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
