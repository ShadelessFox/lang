package com.shade.lang.compiler.parser.node.stmt;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.assembler.Operand;
import com.shade.lang.compiler.assembler.Operation;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Statement;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.node.context.FunctionContext;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.runtime.Machine;
import com.shade.lang.runtime.objects.Chunk;
import com.shade.lang.runtime.objects.function.Guard;
import com.shade.lang.runtime.objects.value.NoneValue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class DeclareFunctionStatement extends Statement {
    private static final Logger LOG = Logger.getLogger(DeclareFunctionStatement.class.getName());

    private final String name;
    private final List<String> arguments;
    private final List<String> boundArguments;
    private final Map<Integer, Integer> boundArgumentsMapping;
    private final BlockStatement body;
    private final boolean variadic;

    public DeclareFunctionStatement(String name, List<String> arguments, List<String> boundArguments, BlockStatement body, boolean variadic, Region region) {
        super(region);
        this.name = name;
        this.arguments = Collections.unmodifiableList(arguments);
        this.boundArguments = Collections.unmodifiableList(boundArguments);
        this.boundArgumentsMapping = new LinkedHashMap<>();
        this.body = body;
        this.variadic = variadic;
    }

    public DeclareFunctionStatement(String name, List<String> arguments, BlockStatement body, boolean variadic, Region region) {
        super(region);
        this.name = name;
        this.arguments = Collections.unmodifiableList(arguments);
        this.boundArguments = Collections.emptyList();
        this.boundArgumentsMapping = new LinkedHashMap<>();
        this.body = body;
        this.variadic = variadic;
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        final Assembler parentAssembler = assembler;

        assembler = new Assembler();
        assembler.addLocation(getRegion().getBegin());

        final AtomicInteger functionLocalsCount = new AtomicInteger();
        final FunctionContext functionContext = new FunctionContext(context);

        functionContext.addListener((name, slot) -> {
            if (functionLocalsCount.get() <= slot) {
                functionLocalsCount.set(slot + 1);
            }
        });

        for (String argument : boundArguments) {
            functionContext.addSlot(argument);
        }

        for (String argument : arguments) {
            functionContext.addSlot(argument);
        }

        body.compile(functionContext, assembler);

        if (!body.isControlFlowReturned()) {
            assembler.emit(Operation.PUSH, Operand.constant(NoneValue.INSTANCE));
            assembler.emit(Operation.RETURN);
        }

        byte functionFlags = 0;

        if (context.getParent() == null) {
            functionFlags |= Chunk.FLAG_MODULE;
        }

        if (variadic) {
            functionFlags |= Chunk.FLAG_VARIADIC;
        }

        final Chunk chunk = new Chunk(
            assembler.assemble().array(),
            assembler.getConstants().toArray(),
            functionContext.getGuards().toArray(new Guard[0]),
            functionFlags,
            (byte) arguments.size(),
            (byte) boundArguments.size(),
            (byte) functionLocalsCount.get(),
            assembler.getComputedLocations()
        );

        parentAssembler.emit(Operation.MAKE_FUNCTION, Operand.constant(name), Operand.constant(chunk));
        parentAssembler.emit(Operation.SET_GLOBAL, Operand.constant(name));

        if (Machine.ENABLE_LOGGING) {
            StringWriter writer = new StringWriter();
            writer.write("Disassembly of function '" + name + "':\n");
            assembler.print(new PrintWriter(writer));
            LOG.info(writer.toString());
        }
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

    public boolean isVariadic() {
        return variadic;
    }
}
