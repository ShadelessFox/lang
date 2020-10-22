package com.shade.lang.parser.node.context;

import com.shade.lang.vm.runtime.Module;

import java.util.Iterator;

public class Context implements Iterable<Context> {
    private final Module module;
    private final Context parent;

    public Context(Context parent) {
        this.module = parent.module;
        this.parent = parent;
    }

    public Context(Module module) {
        this.module = module;
        this.parent = null;
    }

    public Context inner() {
        return new Context(this);
    }

    public boolean hasParent() {
        return parent != null;
    }

    public Context getParent() {
        return parent;
    }

    public Module getModule() {
        return module;
    }

    @Override
    public Iterator<Context> iterator() {
        return new Iterator<Context>() {
            private Context current = Context.this;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public Context next() {
                Context context = current;
                current = context.getParent();
                return context;
            }
        };
    }
}
