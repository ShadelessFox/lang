package com.shade.lang.vm.runtime.function;

import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.Module;

import java.nio.ByteBuffer;
import java.util.Map;

public class Function extends AbstractFunction {
    private final ByteBuffer chunk;
    private final String[] constants;
    private final Map<Integer, Integer> lines;

    public Function(Module module, String name, ByteBuffer chunk, String[] constants, Map<Integer, Integer> lines) {
        super(module, name);
        this.chunk = chunk;
        this.constants = constants;
        this.lines = lines;
    }

    @Override
    public void invoke(Machine machine, int argc) {
        Machine.Frame frame = new Machine.Frame(this, chunk.array(), constants, lines);
        machine.getCallStack().push(frame);
    }
}
