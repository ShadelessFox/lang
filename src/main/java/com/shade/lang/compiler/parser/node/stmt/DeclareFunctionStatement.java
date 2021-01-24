package com.shade.lang.compiler.parser.node.stmt;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.assembler.Operand;
import com.shade.lang.compiler.assembler.Operation;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Statement;
import com.shade.lang.compiler.parser.node.context.ClassContext;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.node.context.FunctionContext;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.runtime.Machine;
import com.shade.lang.runtime.objects.function.Function;
import com.shade.lang.runtime.objects.function.Guard;
import com.shade.lang.runtime.objects.function.RuntimeFunction;
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

        assembler = new Assembler();
        assembler.addLocation(getRegion().getBegin());

        body.compile(functionContext, assembler);

        if (!body.isControlFlowReturned()) {
            assembler.emit(Operation.PUSH, Operand.constant(NoneValue.INSTANCE));
            assembler.emit(Operation.RETURN);
        }

        if (Machine.ENABLE_LOGGING) {
            StringWriter writer = new StringWriter();

            writer.write("Assembly dump for function '" + name + "':\n");
            assembler.print(new PrintWriter(writer));
            LOG.info(writer.toString());
        }

        if (Machine.ENABLE_LOGGING && !functionContext.getGuards().isEmpty()) {
            StringWriter writer = new StringWriter();

            writer.write("Guards of function '" + name + "':\n");
            for (Guard guard : functionContext.getGuards()) {
                writer.write(String.format("  %4d..%-4d -> %4d", guard.getStart(), guard.getEnd(), guard.getOffset()));
                if (guard.getSlot() >= 0) {
                    writer.write(" @ " + guard.getSlot());
                }
                writer.write('\n');
            }
            LOG.info(writer.toString());
        }

        RuntimeFunction function = new RuntimeFunction(
            context.getModule(),
            name,
            variadic ? Function.FLAG_VARIADIC : 0,
            assembler.assemble(),
            assembler.getConstants().toArray(new Object[0]),
            assembler.getComputedLocations(),
            functionContext.getGuards().toArray(new Guard[0]),
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

    public boolean isVariadic() {
        return variadic;
    }
}
