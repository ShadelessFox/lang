package com.shade.lang.runtime.objects;

import com.shade.lang.runtime.objects.function.Guard;
import com.shade.lang.util.annotations.NotNull;
import com.shade.lang.util.annotations.Nullable;
import com.shade.lang.tool.serialization.attributes.Attribute;
import com.shade.lang.tool.serialization.attributes.AttributeDescriptor;

import java.util.Arrays;

public class Chunk extends ScriptObject {
    // @formatter:off
    public static final byte FLAG_VARIADIC = 1;
    public static final byte FLAG_MODULE   = 1 << 1;
    public static final byte FLAG_CLASS    = 1 << 2;
    // @formatter:on

    private final byte[] code;
    private final Object[] constants;
    private final Guard[] guards;
    private final byte flags;
    private final byte arguments;
    private final byte boundArguments;
    private final byte locals;
    private final Attribute<?>[] attributes;

    public Chunk(@NotNull byte[] code, @NotNull Object[] constants, @NotNull Guard[] guards, byte flags, byte arguments, byte boundArguments, byte locals, @NotNull Attribute<?>[] attributes) {
        super(true);
        this.code = code;
        this.constants = constants;
        this.guards = guards;
        this.flags = flags;
        this.arguments = arguments;
        this.boundArguments = boundArguments;
        this.locals = locals;
        this.attributes = attributes;
    }

    @NotNull
    public byte[] getCode() {
        return code;
    }

    @NotNull
    public Object[] getConstants() {
        return constants;
    }

    @NotNull
    public Guard[] getGuards() {
        return guards;
    }

    public byte getFlags() {
        return flags;
    }

    public byte getArguments() {
        return arguments;
    }

    public byte getBoundArguments() {
        return boundArguments;
    }

    public byte getLocals() {
        return locals;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends Attribute<T>> T getSingleAttribute(@NotNull AttributeDescriptor<T> descriptor) {
        return (T) Arrays.stream(attributes)
            .filter(attribute -> attribute.getDescriptor() == descriptor)
            .findFirst().orElse(null);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public <T extends Attribute<T>> T[] getMultipleAttributes(@NotNull AttributeDescriptor<T> descriptor) {
        return (T[]) Arrays.stream(attributes)
            .filter(attribute -> attribute.getDescriptor() == descriptor)
            .toArray();
    }

    @NotNull
    public Attribute<?>[] getFlattenAttributes() {
        return attributes;
    }

    @Override
    public String toString() {
        return "[Code Object " + Integer.toHexString(hashCode()) + "]";
    }
}
