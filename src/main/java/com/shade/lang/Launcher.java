package com.shade.lang;

import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.Module;
import com.shade.lang.vm.runtime.Value;
import com.shade.lang.vm.runtime.function.NativeFunction;

import java.io.File;
import java.net.URL;
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
            machine.call("sandbox", "main");
        }

        System.exit(machine.getStatus());
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
