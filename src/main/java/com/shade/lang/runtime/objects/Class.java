package com.shade.lang.runtime.objects;

import com.shade.lang.util.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Predicate;

public class Class extends ScriptObject {
    private final String name;
    private final Class[] bases;
    private final Class[] resolutionOrder;
    private final List<WeakReference<Class>> subclasses;

    public Class(@NotNull String name, @NotNull Class[] bases) {
        super(false);
        this.name = name;
        this.bases = bases;
        this.resolutionOrder = getResolutionOrder0();
        this.subclasses = new ArrayList<>();

        for (Class base : bases) {
            base.subclasses.add(new WeakReference<>(this));
            attributes.putAll(base.attributes);
        }
    }

    @NotNull
    public Instance instantiate() {
        return new Instance(this);
    }

    public boolean isDerivedFrom(@NotNull Class cls) {
        return Arrays.stream(getResolutionOrder())
            .anyMatch(Predicate.isEqual(cls));
    }

    public boolean isInstance(@NotNull Instance instance) {
        return instance.getBase().isDerivedFrom(this);
    }

    @NotNull
    public Class[] getResolutionOrder() {
        return resolutionOrder;
    }

    @NotNull
    public Class[] getSubclasses() {
        return subclasses.stream()
            .map(Reference::get)
            .filter(Objects::nonNull)
            .toArray(Class[]::new);
    }

    @NotNull
    private Class[] getResolutionOrder0() {
        final Stack<Class> resolutionStack = new Stack<>();
        final Deque<Class> classQueue = new ArrayDeque<>();

        classQueue.offerFirst(this);

        while (!classQueue.isEmpty()) {
            final Class cls = classQueue.pollFirst();

            for (int index = cls.bases.length - 1; index >= 0; index--) {
                classQueue.offerFirst(cls.bases[index]);
            }

            resolutionStack.push(cls);
        }

        return resolutionStack.toArray(new Class[0]);
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public Class[] getBases() {
        return bases;
    }

    @Override
    public String toString() {
        return "[Class " + name + "]";
    }
}
