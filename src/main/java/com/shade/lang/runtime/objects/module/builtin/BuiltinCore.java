package com.shade.lang.runtime.objects.module.builtin;

import com.shade.lang.runtime.Machine;
import com.shade.lang.runtime.frames.Frame;
import com.shade.lang.runtime.objects.ScriptObject;
import com.shade.lang.runtime.objects.module.NativeModule;
import com.shade.lang.runtime.objects.module.NativeModuleProvider;
import com.shade.lang.runtime.objects.value.ArrayValue;
import com.shade.lang.runtime.objects.value.Value;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BuiltinCore extends NativeModule implements NativeModuleProvider {
    public BuiltinCore() {
        super("builtin");
    }

    @Override
    public NativeModule create(Machine machine) {
        return new BuiltinCore();
    }

    @FunctionDescriptor
    public static void println(Machine machine, ScriptObject... args) {
        machine.getOut().println(Stream.of(args).map(Object::toString).collect(Collectors.joining(" ")));
    }

    @FunctionDescriptor
    public static void print(Machine machine, ScriptObject... args) {
        machine.getOut().print(Stream.of(args).map(Object::toString).collect(Collectors.joining(" ")));
    }

    @FunctionDescriptor(name = "to_string")
    public static Object toString(Machine machine, ScriptObject value) {
        return value.toString();
    }

    @FunctionDescriptor(name = "to_array")
    public static Object toArray(Machine machine, ScriptObject... args) {
        return new ArrayValue(args);
    }

    @FunctionDescriptor(name = "add")
    public static Object addItem(Machine machine, ScriptObject[] src, ScriptObject value) {
        ScriptObject[] dst = Arrays.copyOf(src, src.length + 1);
        dst[src.length] = value;
        return new ArrayValue(dst);
    }

    @FunctionDescriptor
    public static void debug(Machine machine, ScriptObject... args) {
        Frame frame = machine.getCallStack().get(machine.getCallStack().size() - 2);
        machine.getErr().print("[" + frame + "] ");
        machine.getErr().println(Stream.of(args).map(Object::toString).collect(Collectors.joining(" ")));
    }

    @FunctionDescriptor(name = "debug_assert")
    public static void debugAssert(Machine machine, ScriptObject... args) {
        for (ScriptObject argument : args) {
            Boolean value = ((Value) argument).getBoolean(machine);
            if (value == null) {
                return;
            }
            if (value == Boolean.FALSE) {
                machine.panic("Assertion failed", true);
                return;
            }
        }
    }

    @FunctionDescriptor
    public static Object panic(Machine machine, ScriptObject payload, ScriptObject recoverable) {
        Boolean value = ((Value) recoverable).getBoolean(machine);
        if (value != null) {
            machine.panic(payload, value);
        }
        return null;
    }
}
