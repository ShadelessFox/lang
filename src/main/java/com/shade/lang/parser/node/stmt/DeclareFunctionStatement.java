package com.shade.lang.parser.node.stmt;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.compiler.Opcode;
import com.shade.lang.parser.ScriptException;
import com.shade.lang.parser.node.Statement;
import com.shade.lang.parser.node.context.ClassContext;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.node.context.FunctionContext;
import com.shade.lang.parser.token.Region;
import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.function.RuntimeFunction;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DeclareFunctionStatement extends Statement {
    private final String name;
    private final List<String> arguments;
    private final List<String> boundArguments;
    private final Map<Integer, Integer> boundArgumentsMapping;
    private final BlockStatement body;

    public DeclareFunctionStatement(String name, List<String> arguments, List<String> boundArguments, BlockStatement body, Region region) {
        super(region);
        this.name = name;
        this.arguments = Collections.unmodifiableList(arguments);
        this.boundArguments = Collections.unmodifiableList(boundArguments);
        this.boundArgumentsMapping = new LinkedHashMap<>();
        this.body = body;
    }

    public DeclareFunctionStatement(String name, List<String> arguments, BlockStatement body, Region region) {
        super(region);
        this.name = name;
        this.arguments = Collections.unmodifiableList(arguments);
        this.boundArguments = Collections.emptyList();
        this.boundArgumentsMapping = new LinkedHashMap<>();
        this.body = body;
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        if (context instanceof ClassContext && arguments.isEmpty()) {
            throw new ScriptException("Class functions must have at least one argument", getRegion());
        }

        for (int index = 0; index < boundArguments.size(); index++) {
            String argument = boundArguments.get(index);
            if (!context.hasSlot(argument)) {
                throw new ScriptException("Cannot capture non-existing variable '" + argument + "'", getRegion());
            }
            boundArgumentsMapping.put(index, context.addSlot(argument));
        }

        AtomicInteger totalSlots = new AtomicInteger();

        FunctionContext functionContext = new FunctionContext(context);
        functionContext.addListener((name, slot) -> {
            if (totalSlots.get() < slot + 1) {
                totalSlots.set(slot + 1);
            }
        });
        for (String argument : boundArguments) {
            functionContext.addSlot(argument);
        }
        for (String argument : arguments) {
            functionContext.addSlot(argument);
        }

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
            arguments.size(),
            boundArguments.size(),
            totalSlots.get()
        );

        context.setAttribute(name, function);
    }

    public String getName() {
        return name;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public List<String> getBoundArguments() {
        return boundArguments;
    }

    public Map<Integer, Integer> getBoundArgumentsMapping() {
        return boundArgumentsMapping;
    }

    public BlockStatement getBody() {
        return body;
    }
}
