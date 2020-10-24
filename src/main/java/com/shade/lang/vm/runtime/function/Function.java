package com.shade.lang.vm.runtime.function;

import com.shade.lang.parser.token.Region;
import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.Module;
import com.shade.lang.vm.runtime.ScriptObject;

import java.nio.ByteBuffer;
import java.util.Map;

public class Function extends AbstractFunction {
    private final ByteBuffer chunk;
    private final String[] constants;
    private final Map<Integer, Region.Span> lines;
    private final int arguments;
    private final int locals;

    public Function(Module module, String name, ByteBuffer chunk, String[] constants, Map<Integer, Region.Span> lines, int arguments, int locals) {
        super(module, name);
        this.chunk = chunk;
        this.constants = constants;
        this.lines = lines;
        this.arguments = arguments;
        this.locals = locals;
    }

    @Override
    public void invoke(Machine machine, int argc) {
        if (arguments != argc) {
            machine.panic("Function '" + getName() + "' accepts " + arguments + " argument(-s) but " + argc + " provided");
        }

        ScriptObject[] objects = new ScriptObject[locals + argc];
        for (int index = argc; index > 0; index--) {
            objects[index - 1] = machine.getOperandStack().pop();
        }

        Machine.Frame frame = new Machine.Frame(this, chunk.array(), constants, objects, lines);
        machine.getCallStack().push(frame);
    }
}
