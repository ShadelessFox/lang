package com.shade.lang.vm.runtime.function;

import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.Module;
import com.shade.lang.vm.runtime.ScriptObject;

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
        ScriptObject[] locals = new ScriptObject[argc];
        for (int index = argc; index > 0; index--) {
            locals[index - 1] = machine.getOperandStack().pop();
        }
        Machine.Frame frame = new Machine.Frame(this, chunk.array(), constants, locals, lines);
        machine.getCallStack().push(frame);
    }
}
