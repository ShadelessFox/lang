package com.shade.lang.vm.runtime.module.builtin;

import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.ScriptObject;
import com.shade.lang.vm.runtime.module.NativeModule;
import com.shade.lang.vm.runtime.module.NativeModuleProvider;
import com.shade.lang.vm.runtime.value.ArrayValue;
import com.shade.lang.vm.runtime.value.NoneValue;
import com.shade.lang.vm.runtime.value.Value;

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

    @FunctionHandler
    public static Object debug(Machine machine, ScriptObject... args) {
        Machine.Frame frame = machine.getCallStack().get(machine.getCallStack().size() - 2);
        machine.getErr().print("[" + frame.getSourceLocation() + "] ");
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
