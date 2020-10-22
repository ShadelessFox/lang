package com.shade.lang.parser.node.stmt;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.gen.Opcode;
import com.shade.lang.parser.node.Visitor;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.node.expr.BinaryExpression;
import com.shade.lang.parser.node.expr.Expression;
import com.shade.lang.parser.token.Region;
import com.shade.lang.parser.token.TokenFlag;

import java.util.ArrayList;
import java.util.List;

public class BranchStatement implements Statement {
    private final Expression condition;
    private final Statement pass;
    private final Statement fail;
    private final Region region;

    public BranchStatement(Expression condition, Statement pass, Statement fail, Region region) {
        this.condition = condition;
        this.pass = pass;
        this.fail = fail;
        this.region = region;
    }

    public Expression getCondition() {
        return condition;
    }

    public Statement getPass() {
        return pass;
    }

    public Statement getFail() {
        return fail;
    }

    @Override
    public boolean isControlFlowReturned() {
        return pass.isControlFlowReturned() && (fail == null || fail.isControlFlowReturned());
    }

    @Override
    public Region getRegion() {
        return region;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void emit(Context context, Assembler assembler) {
        List<Assembler.Label> passLabels = new ArrayList<>();
        List<Assembler.Label> failLabels = new ArrayList<>();
        emitCondition(context, assembler, condition, passLabels, failLabels, true);

        if (failLabels.isEmpty()) {
            failLabels.add(assembler.jump(Opcode.IF_EQ));
        }

        passLabels.forEach(assembler::bind);
        pass.emit(context, assembler);
        Assembler.Label end = pass.isControlFlowReturned() ? null : assembler.jump(Opcode.JUMP);
        failLabels.forEach(assembler::bind);
        if (fail != null) {
            fail.emit(context, assembler);
        }
        assembler.bind(end);
    }

    private void emitCondition(Context context, Assembler assembler, Expression expression, List<Assembler.Label> pass, List<Assembler.Label> fail, boolean top) {
        if (expression instanceof BinaryExpression) {
            BinaryExpression binary = (BinaryExpression) expression;

            if (!binary.getOperator().hasFlag(TokenFlag.BRANCHING)) {
                binary.emit(context, assembler);
                return;
            }

            emitCondition(context, assembler, binary.getLhs(), pass, fail, false);

            switch (binary.getOperator()) {
                case And:
                    fail.add(assembler.jump(Opcode.IF_EQ));
                    break;
                case Or:
                    pass.add(assembler.jump(Opcode.IF_NE));
                    break;
            }

            emitCondition(context, assembler, binary.getRhs(), pass, fail, false);

            switch (binary.getOperator()) {
                case And:
                    if (top) {
                        pass.add(assembler.jump(Opcode.IF_NE));
                    }
                    break;
                case Or:
                    if (top) {
                        fail.add(assembler.jump(Opcode.IF_EQ));
                    }
                    break;
                case Eq:
                    assembler.imm8(Opcode.TEST);
                    fail.add(assembler.jump(Opcode.IF_NE));
                    break;
                case NotEq:
                    assembler.imm8(Opcode.TEST);
                    fail.add(assembler.jump(Opcode.IF_EQ));
                    break;
                case Less:
                    assembler.imm8(Opcode.TEST);
                    fail.add(assembler.jump(Opcode.IF_GE));
                    break;
                case LessEq:
                    assembler.imm8(Opcode.TEST);
                    fail.add(assembler.jump(Opcode.IF_GT));
                    break;
                case Greater:
                    assembler.imm8(Opcode.TEST);
                    fail.add(assembler.jump(Opcode.IF_LE));
                    break;
                case GreaterEq:
                    assembler.imm8(Opcode.TEST);
                    fail.add(assembler.jump(Opcode.IF_LT));
                    break;
            }
        } else {
            expression.emit(context, assembler);
        }
    }
}
