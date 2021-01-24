package com.shade.lang.runtime.objects.module.builtin;

import com.shade.lang.runtime.Machine;
import com.shade.lang.runtime.frames.Frame;
import com.shade.lang.runtime.objects.ScriptObject;
import com.shade.lang.runtime.objects.module.NativeModule;
import com.shade.lang.runtime.objects.module.NativeModuleProvider;
import com.shade.lang.runtime.objects.value.ArrayValue;
import com.shade.lang.runtime.objects.value.NoneValue;
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

    @FunctionHandler
    public static Object println(Machine machine, ScriptObject... args) {
        machine.getOut().println(Stream.of(args).map(Object::toString).collect(Collectors.joining(" ")));
        return NoneValue.INSTANCE;
    }

    @FunctionHandler
    public static Object print(Machine machine, ScriptObject... args) {
        machine.getOut().print(Stream.of(args).map(Object::toString).collect(Collectors.joining(" ")));
        return NoneValue.INSTANCE;
    }

    @FunctionHandler(name = "to_string")
    public static Object toString(Machine machine, ScriptObject value) {
        return value.toString();
    }

    @FunctionHandler(name = "to_array")
    public static Object toArray(Machine machine, ScriptObject... args) {
        return new ArrayValue(args);
    }

    @FunctionHandler(name = "add")
    public static Object addItem(Machine machine, ScriptObject array, ScriptObject value) {
        if (!(array instanceof ArrayValue)) {
            machine.panic("Cannot add item to non-array", true);
            return null;
        }
        ScriptObject[] src = ((ArrayValue) array).getValues();
        ScriptObject[] dst = Arrays.copyOf(src, src.length + 1);
        dst[src.length] = value;
        return new ArrayValue(dst);
    }

    @FunctionHandler
    public static Object debug(Machine machine, ScriptObject... args) {
        Frame frame = machine.getCallStack().get(machine.getCallStack().size() - 2);
        machine.getErr().print("[" + frame + "] ");
        machine.getErr().println(Stream.of(args).map(Object::toString).collect(Collectors.joining(" ")));
        return NoneValue.INSTANCE;
    }

    @FunctionHandler(name = "debug_assert")
    public static Object debugAssert(Machine machine, ScriptObject... args) {
        for (ScriptObject argument : args) {
            Boolean value = ((Value) argument).getBoolean(machine);
            if (value == null) {
                return null;
            }
            if (value == Boolean.FALSE) {
                machine.panic("Assertion failed", true);
                return null;
            }
        }
        return NoneValue.INSTANCE;
    }

    @FunctionHandler
    public static Object panic(Machine machine, ScriptObject message, ScriptObject recoverable) {
        String valueMessage = message.toString();
        Boolean valueRecoverable = ((Value) recoverable).getBoolean(machine);
        if (valueRecoverable != null) {
            machine.panic(valueMessage, valueRecoverable);
        }
        return null;
    }
}