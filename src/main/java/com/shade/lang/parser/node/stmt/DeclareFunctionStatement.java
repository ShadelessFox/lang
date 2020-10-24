package com.shade.lang.parser.node.stmt;

import com.shade.lang.parser.ScriptException;
import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.gen.Opcode;
import com.shade.lang.parser.node.Statement;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;
import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.function.Function;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        Set<String> locals = new HashSet<>();

        assembler = new Assembler(Machine.MAX_CODE_SIZE);
        assembler.span(getRegion().getBegin());

        Context functionContext = context.wrap();
        functionContext.setObserver(locals::add);
        functionContext.makeSlots(arguments);

        body.compile(functionContext, assembler);

        if (!body.isControlFlowReturned()) {
            // TODO: Need a better way to emit implicit return
            assembler.imm8(Opcode.PUSH_INT);
            assembler.imm32(0);
            assembler.imm8(Opcode.RET);
        }

        System.out.println("ASSEMBLY DEBUG FOR FUNCTION '" + name + "':");
        System.out.println(locals);
        assembler.dump(System.out);

        Function function = new Function(
            context.getModule(),
            name,
            assembler.getBuffer(),
            assembler.getConstants(),
            assembler.getLines(),
            arguments.size(), locals.size() - arguments.size());

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
