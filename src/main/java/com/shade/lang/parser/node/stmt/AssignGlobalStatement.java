package com.shade.lang.parser.node.stmt;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.gen.Opcode;
import com.shade.lang.parser.node.Visitor;
import com.shade.lang.parser.node.expr.Expression;
import com.shade.lang.vm.runtime.Module;

public class AssignGlobalStatement implements Statement {
    private final String name;
    private final Expression value;

    public AssignGlobalStatement(String name, Expression value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public boolean isControlFlowReturned() {
        return false;
    }

    @Override
    public void emit(Module module, Assembler assembler) {
        value.emit(module, assembler);
        assembler.imm8(Opcode.SET_GLOBAL);
        assembler.imm32(assembler.constant(name));
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
