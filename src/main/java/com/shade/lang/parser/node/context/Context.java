package com.shade.lang.parser.node.context;

import com.shade.lang.vm.runtime.Module;

import java.util.*;
import java.util.function.BiConsumer;

public class Context implements Iterable<Context> {
    private final Module module;
    private final Context parent;
    private final List<String> slots;
    private BiConsumer<Integer, String> observer;
    private int slotsCount;

    public Context(Context parent, int slotsNextId) {
        this.module = parent.module;
        this.parent = parent;
        this.slots = new ArrayList<>();
        this.slotsCount = slotsNextId;
    }

    public Context(Module module, int slotsNextId) {
        this.module = module;
        this.parent = null;
        this.slots = new ArrayList<>();
        this.slotsCount = slotsNextId;
    }

    public Context wrap() {
        Context context = new Context(this, slotsCount);
        context.setObserver(observer);
        return context;
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

    public Module getModule() {
        return module;
    }

    public Context getParent() {
        return parent;
    }

    public boolean hasSlot(String name) {
        if (slots.contains(name)) {
            return true;
        }

        return parent != null && parent.hasSlot(name);
    }

    public int addSlot(String name) {
        for (Context current = this; current != null; current = current.parent) {
            if (current.slots.contains(name)) {
                int slotsCount = current.slots.indexOf(name);
                if (current.parent != null) {
                    slotsCount += current.parent.slotsCount;
                }
                return slotsCount;
            }
        }

        if (slotsCount < 255) {
            slots.add(name);
            observer.accept(slotsCount, name);
            return slotsCount++;
        }

        throw new RuntimeException("Locals are limited to 255 per context");
    }

    public int[] addSlots(Collection<String> names) {
        return names.stream().mapToInt(this::addSlot).toArray();
    }

    public int getSlotsCount() {
        return slotsCount;
    }

    public BiConsumer<Integer, String> getObserver() {
        return observer;
    }

    public void setObserver(BiConsumer<Integer, String> observer) {
        this.observer = observer;
    }
}
