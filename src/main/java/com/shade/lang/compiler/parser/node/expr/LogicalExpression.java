package com.shade.lang.compiler.parser.node.expr;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.assembler.Operation;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Expression;
import com.shade.lang.compiler.parser.node.Node;
import com.shade.lang.compiler.parser.node.Statement;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.node.visitor.Visitor;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.compiler.parser.token.TokenKind;
import com.shade.lang.util.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LogicalExpression extends Expression {
    private final Expression lhs;
    private final Expression rhs;
    private final TokenKind operator;
    private Node pass;
    private Node fail;

    public LogicalExpression(Expression lhs, Expression rhs, TokenKind operator, Region region) {
        super(region);
        this.lhs = lhs;
        this.rhs = rhs;
        this.operator = operator;
        this.pass = null;
        this.fail = null;
    }

    public LogicalExpression(Expression lhs, Expression rhs, TokenKind operator, Node pass, Node fail, Region region) {
        super(region);
        this.lhs = lhs;
        this.rhs = rhs;
        this.operator = operator;
        this.pass = pass;
        this.fail = fail;
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        final List<Assembler.Label> passLabels = new ArrayList<>();
        final List<Assembler.Label> failLabels = new ArrayList<>();

        compile0(context, assembler, this, passLabels, failLabels, pass == null);

        if (pass != null) {
            failLabels.add(assembler.jump(Operation.JUMP_IF_FALSE));
            assembler.addLocation(getRegion().getBegin());
        }

        passLabels.forEach(assembler::bind);

        if (pass != null) {
            pass.compile(context, assembler);
        }

        failLabels.forEach(assembler::bind);

        if (pass != null) {
            final Assembler.Label end = pass instanceof Statement && !((Statement) pass).isControlFlowReturned()
                ? assembler.jump(Operation.JUMP)
                : null;

            if (fail != null) {
                fail.compile(context, assembler);
            }

            assembler.bind(end);
        }
    }

    private void compile0(Context context, Assembler assembler, Node node, List<Assembler.Label> passLabels, List<Assembler.Label> failLabels, boolean preserveResults) throws ScriptException {
        if (node instanceof LogicalExpression) {
            final Expression lhs = ((LogicalExpression) node).getLhs();
            final Expression rhs = ((LogicalExpression) node).getRhs();

            compile0(context, assembler, lhs, passLabels, failLabels, preserveResults);

            switch (operator) {
                case And:
                    failLabels.add(assembler.jump(preserveResults ? Operation.JUMP_IF_FALSE_OR_POP : Operation.JUMP_IF_FALSE));
                    assembler.addLocation(getRegion().getBegin());
                    break;
                case Or:
                    passLabels.add(assembler.jump(preserveResults ? Operation.JUMP_IF_TRUE_OR_POP : Operation.JUMP_IF_TRUE));
                    assembler.addLocation(getRegion().getBegin());
                    break;
                default:
                    throw new AssertionError("Unsupported logical operator: " + operator);
            }

            compile0(context, assembler, rhs, passLabels, failLabels, preserveResults);
        } else {
            node.compile(context, assembler);
        }
    }

    @NotNull
    @Override
    public Expression accept(@NotNull Visitor visitor) {
        if (visitor.enterLogicalExpression(this)) {
            final Expression lhs = this.lhs.accept(visitor);
            final Expression rhs = this.rhs.accept(visitor);
            final Node pass = this.pass == null ? null : this.pass.accept(visitor);
            final Node fail = this.fail == null ? null : this.fail.accept(visitor);

            if (lhs != this.lhs || rhs != this.rhs || pass != this.pass || fail != this.fail) {
                return visitor.leaveLogicalExpression(new LogicalExpression(lhs, rhs, operator, pass, fail, getRegion()));
            } else {
                return visitor.leaveLogicalExpression(this);
            }
        }

        return this;
    }

    public Expression getLhs() {
        return lhs;
    }

    public Expression getRhs() {
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
