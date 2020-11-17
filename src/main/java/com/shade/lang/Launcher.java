package com.shade.lang;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.parser.node.stmt.ImportStatement;
import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.Class;
import com.shade.lang.vm.runtime.Module;
import com.shade.lang.vm.runtime.ScriptObject;
import com.shade.lang.vm.runtime.function.Function;
import com.shade.lang.vm.runtime.function.NativeFunction;
import com.shade.lang.vm.runtime.value.NoneValue;
import com.shade.lang.vm.runtime.value.Value;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Launcher {
    public static void main(String[] args) throws Exception {
        Machine machine = new Machine();
        machine.getSearchRoots().add(Paths.get("src/main/resources"));
        machine.load(Builtin.INSTANCE);
        machine.load(Paths.get("src/main/resources/sandbox.ash"));

        if (!machine.isHalted()) {
            machine.call("sandbox", "main");
        }

//        try (DataOutputStream stream = new DataOutputStream(new FileOutputStream("sandbox.bin"))) {
//            export(machine.getModules().get("sandbox"), stream);
//        }

        System.exit(machine.getStatus());
    }

    private static void export(Module module, DataOutputStream stream) throws IOException {
        final List<RuntimeFunction> functions = module.getAttributes()
            .values().stream()
            .filter(x -> x instanceof RuntimeFunction)
            .map(x -> (RuntimeFunction) x)
            .sorted(Comparator.comparing(Function::getName))
            .collect(Collectors.toList());

        stream.write(new byte[]{'A', 'S', 'H', 0});
        stream.writeLong(0);

        stream.writeInt(module.getImports().size());
        for (ImportStatement statement : module.getImports()) {
            stream.writeUTF(statement.getName());
            stream.writeUTF(statement.getAlias());
        }

        stream.writeInt(functions.size());
        for (RuntimeFunction function : functions) {
            stream.writeUTF(function.getName());
            stream.write(function.getArgumentsCount());
            stream.write(function.getBoundArgumentsCount());
            stream.write(function.getLocalsCount());

            byte[] chunk = function.getChunk().array();
            stream.writeInt(chunk.length);
            stream.write(chunk);

            stream.writeInt(function.getConstants().length);
            for (Object constant : function.getConstants()) {
                // TODO: Write constants along with its type tag
                throw new IllegalArgumentException("Unsupported constant type: " + constant.getClass());
            }

            stream.writeInt(function.getGuards().length);
            for (Assembler.Guard guard : function.getGuards()) {
                stream.writeInt(guard.getStart());
                stream.writeInt(guard.getEnd());
                stream.writeInt(guard.getOffset());
                stream.write(guard.getSlot());
            }
        }
    }

    private static class Builtin extends Module {
        public static final Builtin INSTANCE = new Builtin();

        public Builtin() {
            super("builtin", "<builtin>");

            setAttribute("to_string", new NativeFunction(this, "to_string", 1, 0, (machine, args) -> args[0].toString()));

            setAttribute("print", new NativeFunction(this, "print", 0, Function.FLAG_VARIADIC, (machine, args) -> {
                machine.getOut().print(Stream.of(args).map(Object::toString).collect(Collectors.joining(" ")));
                return NoneValue.INSTANCE;
            }));

            setAttribute("println", new NativeFunction(this, "println", 0, Function.FLAG_VARIADIC, (machine, args) -> {
                machine.getOut().println(Stream.of(args).map(Object::toString).collect(Collectors.joining(" ")));
                return NoneValue.INSTANCE;
            }));

            setAttribute("debug", new NativeFunction(this, "debug", 0, Function.FLAG_VARIADIC, (machine, args) -> {
                Machine.Frame frame = machine.getCallStack().get(machine.getCallStack().size() - 2);
                machine.getErr().print("[" + frame.getSourceLocation() + "] ");
                machine.getErr().println(Stream.of(args).map(Object::toString).collect(Collectors.joining(" ")));
                return NoneValue.INSTANCE;
            }));

            setAttribute("panic", new NativeFunction(this, "panic", 2, 0, (machine, args) -> {
                String message = (String) ((Value) args[0]).getValue();
                boolean recoverable = (Integer) ((Value) args[1]).getValue() != 0;
                machine.panic(message, recoverable);
                return null;
            }));

            setAttribute("debug_assert", new NativeFunction(this, "debug_assert", 1, 0, (machine, args) -> {
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
            }));
        }
    }
}
