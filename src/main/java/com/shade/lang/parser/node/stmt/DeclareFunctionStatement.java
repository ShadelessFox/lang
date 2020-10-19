package com.shade.lang.parser.node.stmt;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.node.Visitor;
import com.shade.lang.vm.runtime.Module;

import java.util.Collections;
import java.util.List;

public class DeclareFunctionStatement implements Statement {
    private final String name;
    private final List<String> arguments;
    private final BlockStatement body;

    public DeclareFunctionStatement(String name, List<String> arguments, BlockStatement body) {
        this.name = name;
        this.arguments = Collections.unmodifiableList(arguments);
        this.body = body;
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

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void emit(Module module, Assembler assembler) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean isControlFlowReturned() {
        return false;
    }
}
