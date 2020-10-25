package com.shade.lang.vm.runtime.function;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.token.Region;
import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.Module;
import com.shade.lang.vm.runtime.ScriptObject;

import java.nio.ByteBuffer;
import java.util.Map;

public class RuntimeFunction extends Function {
    private final ByteBuffer chunk;
    private final String[] constants;
    private final Map<Integer, Region.Span> lines;
    private final Assembler.Guard[] guards;
    private final int arguments;
    private final int locals;

    public RuntimeFunction(Module module, String name, ByteBuffer chunk, String[] constants, Map<Integer, Region.Span> lines, Assembler.Guard[] guards, int arguments, int locals) {
        super(module, name);
        this.chunk = chunk;
        this.constants = constants;
        this.lines = lines;
        this.guards = guards;
        this.arguments = arguments;
        this.locals = locals;
    }

    @Override
    public void invoke(Machine machine, int argc) {
        if (arguments != argc) {
            machine.panic("Function '" + getName() + "' accepts " + arguments + " argument(-s) but " + argc + " provided", true);
            return;
        }

        ScriptObject[] objects = new ScriptObject[locals + argc];
        for (int index = argc; index > 0; index--) {
            objects[index - 1] = machine.getOperandStack().pop();
        }

        Machine.Frame frame = new Machine.Frame(this, chunk.array(), constants, objects, lines);
        machine.getCallStack().push(frame);
    }

    public Assembler.Guard[] getGuards() {
        return guards;
    }
}
