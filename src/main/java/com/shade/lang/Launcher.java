package com.shade.lang;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.parser.node.stmt.ImportStatement;
import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.Module;
import com.shade.lang.vm.runtime.Value;
import com.shade.lang.vm.runtime.function.NativeFunction;
import com.shade.lang.vm.runtime.function.RuntimeFunction;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Launcher {
    public static void main(String[] args) throws Exception {
        Machine machine = new Machine();
        machine.load(Builtin.INSTANCE);

        URL path = Launcher.class.getClassLoader().getResource("source.lang");
        if (path == null) {
            throw new RuntimeException("Cannot find source file");
        }

        machine.load("sandbox", new File(path.toURI()));

        if (!machine.isHalted()) {
            try (DataOutputStream stream = new DataOutputStream(new FileOutputStream("sandbox.bin"))) {
                export(machine.getModules().get("sandbox"), stream);
            }

            machine.call("sandbox", "main");
        }

        System.exit(machine.getStatus());
    }

    private static void export(Module module, DataOutputStream stream) throws IOException {
        final List<RuntimeFunction> functions = module.getAttributes()
            .values().stream()
            .filter(x -> x instanceof RuntimeFunction)
            .map(x -> (RuntimeFunction) x)
            .collect(Collectors.toList());

        stream.write(new byte[]{ 'A', 'S', 'H', 0 });
        stream.writeLong(0);

        stream.writeInt(module.getImports().size());
        for (ImportStatement statement : module.getImports()) {
            stream.writeUTF(statement.getName());
            stream.writeUTF(statement.getAlias());
        }

        stream.writeInt(functions.size());
        for (RuntimeFunction function : functions) {
            stream.writeUTF(function.getName());
            stream.write(function.getArguments());
            stream.write(function.getLocals());

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

            setAttribute("print", new NativeFunction(this, "print", (machine, args) -> {
                machine.getOut().println(Stream.of(args).map(Object::toString).collect(Collectors.joining(" ")));
                return new Value(0);
            }));

            setAttribute("assert0", new NativeFunction(this, "assert", (machine, args) -> {
                if (Stream.of(args).map(x -> (Value) x).anyMatch(x -> x == null || (int) x.getValue() == 0)) {
                    machine.panic("Assertion failed", true);
                    return null;
                }
                return new Value(0);
            }));
        }
    }
}
