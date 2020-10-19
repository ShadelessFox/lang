package com.shade.lang.parser.node.stmt;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.gen.Opcode;
import com.shade.lang.parser.node.Visitor;
import com.shade.lang.parser.node.expr.Expression;
import com.shade.lang.vm.runtime.Module;

public class BranchStatement implements Statement {
    private final Expression cond;
    private final Statement pass;
    private final Statement fail;

    public BranchStatement(Expression condition, Statement pass, Statement fail) {
        this.cond = condition;
        this.pass = pass;
        this.fail = fail;
    }

    public Expression getCondition() {
        return cond;
    }

    public Statement getPass() {
        return pass;
    }

    public Statement getFail() {
        return fail;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void emit(Module module, Assembler assembler) {
        cond.emit(module, assembler);
        Assembler.Label alternate = assembler.jump(Opcode.IF_EQ);
        pass.emit(module, assembler);
        Assembler.Label end = pass.isControlFlowReturned() ? null : assembler.jump(Opcode.JUMP);
        assembler.bind(alternate);
        fail.emit(module, assembler);
        assembler.bind(end);
    }

    @Override
    public boolean isControlFlowReturned() {
        return pass.isControlFlowReturned() && fail.isControlFlowReturned();
    }
}
