package com.shade.lang.parser.node.stmt;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.gen.Opcode;
import com.shade.lang.parser.node.Visitor;
import com.shade.lang.parser.node.expr.Expression;
import com.shade.lang.vm.runtime.Module;

public class AssignAttributeStatement implements Statement {
    private final Expression target;
    private final String name;
    private final Expression value;

    public AssignAttributeStatement(Expression target, String name, Expression value) {
        this.target = target;
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Expression getValue() {
        return value;
    }

    public Expression getTarget() {
        return target;
    }

    @Override
    public boolean isControlFlowReturned() {
        return false;
    }

    @Override
    public void emit(Module module, Assembler assembler) {
        target.emit(module, assembler);
        value.emit(module, assembler);
        assembler.imm8(Opcode.SET_ATTRIBUTE);
        assembler.imm32(assembler.constant(name));
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
