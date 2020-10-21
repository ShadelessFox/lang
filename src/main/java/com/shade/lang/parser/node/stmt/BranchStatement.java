package com.shade.lang.parser.node.stmt;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.gen.Opcode;
import com.shade.lang.parser.node.Visitor;
import com.shade.lang.parser.node.expr.BinaryExpression;
import com.shade.lang.parser.node.expr.Expression;
import com.shade.lang.vm.runtime.Module;

import java.util.ArrayList;
import java.util.List;

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
        List<Assembler.Label> passLabels = new ArrayList<>();
        List<Assembler.Label> failLabels = new ArrayList<>();
        emitCondition(module, assembler, cond, passLabels, failLabels, true);

        if (failLabels.isEmpty()) {
            failLabels.add(assembler.jump(Opcode.IF_EQ));
        }

        passLabels.forEach(assembler::bind);
        pass.emit(module, assembler);
        Assembler.Label end = pass.isControlFlowReturned() ? null : assembler.jump(Opcode.JUMP);
        failLabels.forEach(assembler::bind);
        fail.emit(module, assembler);
        assembler.bind(end);
    }

    @Override
    public boolean isControlFlowReturned() {
        return pass.isControlFlowReturned() && fail.isControlFlowReturned();
    }

    private void emitCondition(Module module, Assembler assembler, Expression expression, List<Assembler.Label> pass, List<Assembler.Label> fail, boolean top) {
        if (expression instanceof BinaryExpression) {
            BinaryExpression binary = (BinaryExpression) expression;
            emitCondition(module, assembler, binary.getLhs(), pass, fail, false);
            switch (binary.getOperator()) {
                case And: fail.add(assembler.jump(Opcode.IF_EQ)); break;
                case Or:  pass.add(assembler.jump(Opcode.IF_NE)); break;
                default:  throw new RuntimeException("Unsupported operator: " + binary.getOperator());
            }
            emitCondition(module, assembler, binary.getRhs(), pass, fail, false);
            if (top) {
                switch (binary.getOperator()) {
                    case And: pass.add(assembler.jump(Opcode.IF_NE)); break;
                    case Or:  fail.add(assembler.jump(Opcode.IF_EQ)); break;
                    default:  throw new RuntimeException("Unsupported operator: " + binary.getOperator());
                }
            }
        } else {
            expression.emit(module, assembler);
        }
    }
}
