package com.shade.lang.parser.node.context;

import com.shade.lang.vm.runtime.module.Module;
import com.shade.lang.vm.runtime.ScriptObject;

import java.io.Closeable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class Context implements Closeable, Iterable<Context.Scope> {
    protected final Module module;
    protected final Context parent;
    protected final Stack<Scope> scopes;
    protected final List<BiConsumer<String, Integer>> listeners;
    protected int nextLocalSlot;

    public Context(Context parent) {
        this.module = parent.module;
        this.parent = parent;
        this.scopes = new Stack<>();
        this.scopes.add(new Scope(this, 0));
        this.listeners = new ArrayList<>();
        this.listeners.addAll(this.parent.listeners);
        this.nextLocalSlot = this.parent.nextLocalSlot;
    }

    public Context(Module module) {
        this.module = module;
        this.parent = null;
        this.scopes = new Stack<>();
        this.scopes.add(new Scope(this, 0));
        this.listeners = new ArrayList<>();
        this.nextLocalSlot = 0;
    }

    @SuppressWarnings("unchecked")
    public <T extends Context> T unwrap(Class<? extends T> clazz) {
        for (Context context = this; context != null; context = context.parent) {
            if (clazz.isAssignableFrom(context.getClass())) {
                return (T) context;
            }
        }

        return null;
    }

    public Context enter() {
        scopes.push(new Scope(this, nextLocalSlot));
        return this;
    }

    public void leave() {
        nextLocalSlot = scopes.pop().getFirstSlotIndex();
    }

    public int addSlot(String name) {
        for (Scope scope : this) {
            if (scope.hasLocal(name)) {
                return scope.getFirstSlotIndex() + scope.getLocals().indexOf(name);
            }
        }

        scopes.peek().getLocals().add(name);

        for (BiConsumer<String, Integer> listener : listeners) {
            listener.accept(name, nextLocalSlot);
        }

        return nextLocalSlot++;
    }

    public boolean hasSlot(String name) {
        for (Scope scope : this) {
            if (scope.hasLocal(name)) {
                return true;
            }
        }

        return false;
    }

    public void addListener(BiConsumer<String, Integer> listener) {
        listeners.add(listener);
    }

    public void setAttribute(String name, ScriptObject value) {
        module.setAttribute(name, value);
    }

    public ScriptObject getAttribute(String name) {
        return module.getAttribute(name);
    }

    public Map<String, ScriptObject> getAttributes() {
        return module.getAttributes();
    }

    public Module getModule() {
        return module;
    }

    public Context getParent() {
        return parent;
    }

    public Stack<Scope> getScopes() {
        return scopes;
    }

    @Override
    public void close() {
        leave();
    }

    @Override
    public Iterator<Scope> iterator() {
        return new Iterator<Scope>() {
            private Context context = Context.this;
            private int index = context.scopes.size() - 1;

            @Override
            public boolean hasNext() {
                return context != null && index >= 0;
            }

            @Override
            public Scope next() {
                Scope scope = context.scopes.get(index--);

                if (index < 0) {
                    context = context.parent;

                    if (context != null) {
                        index = context.scopes.size() - 1;
                    }
                }

                return scope;
            }
        };
    }

    @Override
    public String toString() {
        return "Context[" + scopes.stream().map(Objects::toString).collect(Collectors.joining(", ")) + "]";
    }

    public static class Scope {
        private final Context context;
        private final List<String> locals;
        private final int firstSlotIndex;

        public Scope(Context context, int firstSlotIndex) {
            this.context = context;
            this.locals = new ArrayList<>();
            this.firstSlotIndex = firstSlotIndex;
        }

        public boolean hasLocal(String name) {
            return locals.contains(name);
        }

        public boolean addLocal(String name) {
            return locals.add(name);
        }

        public Context getContext() {
            return context;
        }

        public List<String> getLocals() {
            return locals;
        }

        public int getFirstSlotIndex() {
            return firstSlotIndex;
        }

        @Override
        public String toString() {
            return "Scope[" + String.join(", ", locals) + "]";
        }
    }
}
