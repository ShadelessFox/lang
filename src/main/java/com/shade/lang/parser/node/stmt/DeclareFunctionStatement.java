package com.shade.lang.parser.node.stmt;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.compiler.Opcode;
import com.shade.lang.parser.ScriptException;
import com.shade.lang.parser.node.Statement;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;
import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.function.RuntimeFunction;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DeclareFunctionStatement extends Statement {
    private final String name;
    private final List<String> arguments;
    private final BlockStatement body;

    public DeclareFunctionStatement(String name, List<String> arguments, BlockStatement body, Region region) {
        super(region);
        this.name = name;
        this.arguments = Collections.unmodifiableList(arguments);
        this.body = body;
    }

    @Override
    public boolean isControlFlowReturned() {
        return false;
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        AtomicInteger totalSlots = new AtomicInteger();

        Context functionContext = context.wrap();
        functionContext.setObserver((slot, name) -> {
            if (totalSlots.get() < slot + 1) {
                totalSlots.set(slot + 1);
            }
        });
        functionContext.addSlots(arguments);

        assembler = new Assembler(Machine.MAX_CODE_SIZE);
        assembler.addTraceLine(getRegion().getBegin());
        assembler.addDebugLine(body.getRegion().getBegin(), "Function entry");

        body.compile(functionContext, assembler);

        assembler.addDebugLine(getRegion().getEnd(), "Function end");

        if (!body.isControlFlowReturned()) {
            assembler.imm8(Opcode.PUSH_INT);
            assembler.imm32(0);
            assembler.imm8(Opcode.RET);
        }

        RuntimeFunction function = new RuntimeFunction(
            context.getModule(),
            name,
            assembler.build(),
            assembler.getConstants(),
            assembler.getTraceLines(),
            assembler.getGuards(),
            arguments.size(), totalSlots.get());

        context.getModule().setAttribute(name, function);
    }

    public String getName() {
        return name;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public BlockStatement getBody() {
        return body;
    }
}
