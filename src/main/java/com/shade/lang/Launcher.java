package com.shade.lang;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.parser.node.stmt.ImportStatement;
import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.Module;
import com.shade.lang.vm.runtime.value.Value;
import com.shade.lang.vm.runtime.function.Function;
import com.shade.lang.vm.runtime.function.NativeFunction;
import com.shade.lang.vm.runtime.function.RuntimeFunction;

import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
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
            for (String constant : function.getConstants()) {
                stream.writeUTF(constant == null ? "" : constant);
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

            setAttribute("to_string", new NativeFunction(this, "to_string", (machine, args) -> args[0].toString()));

            setAttribute("print", new NativeFunction(this, "print", (machine, args) -> {
                machine.getOut().print(Stream.of(args).map(Object::toString).collect(Collectors.joining(" ")));
                return 0;
            }));

            setAttribute("println", new NativeFunction(this, "println", (machine, args) -> {
                machine.getOut().println(Stream.of(args).map(Object::toString).collect(Collectors.joining(" ")));
                return 0;
            }));

            setAttribute("debug", new NativeFunction(this, "debug", (machine, args) -> {
                Machine.Frame frame = machine.getCallStack().get(machine.getCallStack().size() - 2);
                machine.getErr().print("[" + frame.getSourceLocation() + "] ");
                machine.getErr().println(Stream.of(args).map(Object::toString).collect(Collectors.joining(" ")));
                return 0;
            }));

            setAttribute("panic", new NativeFunction(this, "panic", (machine, args) -> {
                String message = (String) ((Value) args[0]).getValue();
                boolean recoverable = (Integer) ((Value) args[1]).getValue() != 0;
                machine.panic(message, recoverable);
                return null;
            }));

            setAttribute("debug_assert", new NativeFunction(this, "assert", (machine, args) -> {
                if (Stream.of(args).map(x -> (Value) x).anyMatch(x -> x == null || (Integer) x.getValue() == 0)) {
                    machine.panic("Assertion failed", true);
                    return null;
                }
                return 0;
            }));
        }
    }
}
