package com.shade.lang;

import com.shade.lang.parser.ParseException;
import com.shade.lang.parser.Parser;
import com.shade.lang.parser.Tokenizer;
import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.node.stmt.Statement;
import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.Module;
import com.shade.lang.vm.runtime.function.Function;
import com.shade.lang.vm.runtime.function.NativeFunction;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Launcher {
    public static void main(String[] args) {
        Machine machine = new Machine();

        machine.load(Builtin.INSTANCE);
        machine.load(Sandbox.INSTANCE);

        machine.call("builtin", "print", "hi there");
        machine.call("sandbox", "main");
    }

    private static class Sandbox extends Module {
        public static final Sandbox INSTANCE = new Sandbox();

        public Sandbox() {
            super("sandbox", "<sandbox>");

            try {
                Tokenizer tokenizer = new Tokenizer(new StringReader("{ builtin.print = builtin.asdads; builtin.print(builtin.add(5, 7)); return 0; }"));
                Parser parser = new Parser(tokenizer);

                Assembler assembler = new Assembler(Machine.MAX_CODE_SIZE);

                Statement program = parser.declarativeStatement();
                program.emit(this, assembler);

                assembler.dump(System.out);

                Map<Integer, Integer> lines = new HashMap<>();
                lines.put(15, 1);

                getAttributes().put("main", new Function(this, "main", assembler.getBuffer(), assembler.getConstants().toArray(new String[0]), lines));
            } catch (IOException | ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class Builtin extends Module {
        public static final Builtin INSTANCE = new Builtin();

        public Builtin() {
            super("builtin", "<builtin>");

            getAttributes().put("print", new NativeFunction(this, "print", args -> {
                System.out.println(Stream.of(args).map(Object::toString).collect(Collectors.joining(" ")));
                return null;
            }));

            getAttributes().put("add", new NativeFunction(this, "add", args -> (int) args[0] + (int) args[1]));
        }
    }
}
