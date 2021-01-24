package com.shade.lang.runtime.objects;

import com.shade.lang.runtime.objects.function.Guard;
import com.shade.lang.util.Pair;
import com.shade.lang.util.annotations.NotNull;

import java.util.Map;

public class Chunk extends ScriptObject {
    // @formatter:off
    public static final byte FLAG_VARIADIC = 1;
    public static final byte FLAG_MODULE   = 1 << 1;
    // @formatter:on

    private final byte[] code;
    private final Object[] constants;
    private final Guard[] guards;
    private final byte flags;
    private final byte arguments;
    private final byte boundArguments;
    private final byte locals;
    private final Map<Integer, Pair<Short, Short>> locations;

    public Chunk(@NotNull byte[] code, @NotNull Object[] constants, @NotNull Guard[] guards, byte flags, byte arguments, byte boundArguments, byte locals, @NotNull Map<Integer, Pair<Short, Short>> locations) {
        super(true);
        this.code = code;
        this.constants = constants;
        this.guards = guards;
        this.flags = flags;
        this.arguments = arguments;
        this.boundArguments = boundArguments;
        this.locals = locals;
        this.locations = locations;
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

    @NotNull
    public Map<Integer, Pair<Short, Short>> getLocations() {
        return locations;
    }

    @Override
    public String toString() {
        return "[Code Object " + Integer.toHexString(hashCode()) + "]";
    }
}
