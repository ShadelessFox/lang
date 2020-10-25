package com.shade.lang.parser.node.expr;

import com.shade.lang.parser.ScriptException;
import com.shade.lang.compiler.Assembler;
import com.shade.lang.compiler.Opcode;
import com.shade.lang.parser.node.Expression;
import com.shade.lang.parser.node.Node;
import com.shade.lang.parser.node.Statement;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;
import com.shade.lang.parser.token.TokenKind;

import java.util.ArrayList;
import java.util.List;

public class LogicalExpression extends Expression {
    private final Node lhs;
    private final Node rhs;
    private final TokenKind operator;
    private Node pass;
    private Node fail;

    public LogicalExpression(Expression lhs, Expression rhs, TokenKind operator, Node pass, Node fail, Region region) {
        super(region);
        this.lhs = lhs;
        this.rhs = rhs;
        this.operator = operator;
        this.pass = pass;
        this.fail = fail;
    }

    public LogicalExpression(Expression lhs, Expression rhs, TokenKind operator, Region region) {
        super(region);
        this.lhs = lhs;
        this.rhs = rhs;
        this.operator = operator;
        this.pass = new LoadConstantExpression<>(1, region);
        this.fail = new LoadConstantExpression<>(0, region);
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        final List<Assembler.Label> passLabels = new ArrayList<>();
        final List<Assembler.Label> failLabels = new ArrayList<>();

        compile(context, assembler, this, pass, fail, passLabels, failLabels, true);

        passLabels.forEach(assembler::bind);
        pass.compile(context, assembler);

        Assembler.Label end = null;
        if (!(lhs instanceof Statement) || !((Statement) lhs).isControlFlowReturned()) {
            end = assembler.jump(Opcode.JUMP);
        }

        failLabels.forEach(assembler::bind);
        if (fail != null) {
            fail.compile(context, assembler);
        }

        assembler.bind(end);
    }

    private void compile(Context context, Assembler assembler, Node node, Node pass, Node fail, List<Assembler.Label> passLabels, List<Assembler.Label> failLabels, boolean root) throws ScriptException {
        if (node instanceof LogicalExpression) {
            LogicalExpression logical = (LogicalExpression) node;
            logical.setPass(pass);
            logical.setFail(fail);

            compile(context, assembler, logical.lhs, pass, fail, passLabels, failLabels, false);

            switch (logical.operator) {
                case And:
                    failLabels.add(assembler.jump(Opcode.JUMP_IF_FALSE));
                    break;
                case Or:
                    passLabels.add(assembler.jump(Opcode.JUMP_IF_TRUE));
                    break;
            }

            compile(context, assembler, logical.rhs, pass, fail, passLabels, failLabels, false);

            if (root) {
                failLabels.add(assembler.jump(Opcode.JUMP_IF_FALSE));
            }
        } else {
            node.compile(context, assembler);
        }
    }

    public Node getLhs() {
        return lhs;
    }

    public Node getRhs() {
        return rhs;
    }

    public TokenKind getOperator() {
        return operator;
    }

    public Node getPass() {
        return pass;
    }

    public void setPass(Node pass) {
        this.pass = pass;
    }

    public Node getFail() {
        return fail;
    }

    public void setFail(Node fail) {
        this.fail = fail;
    }
}
