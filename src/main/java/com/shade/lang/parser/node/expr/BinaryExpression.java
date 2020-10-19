package com.shade.lang.parser.node.expr;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.gen.Opcode;
import com.shade.lang.parser.node.Visitor;
import com.shade.lang.parser.token.TokenKind;
import com.shade.lang.vm.runtime.Module;

public class BinaryExpression implements Expression {
    private final Expression lhs;
    private final Expression rhs;
    private final TokenKind operator;

    public BinaryExpression(Expression lhs, Expression rhs, TokenKind operator) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.operator = operator;
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

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void emit(Module module, Assembler assembler) {
        lhs.emit(module, assembler);
        rhs.emit(module, assembler);

        switch (operator) {
            case Add:
                assembler.imm8(Opcode.ADD);
                break;
            case Sub:
                assembler.imm8(Opcode.SUB);
                break;
            case Mul:
                assembler.imm8(Opcode.MUL);
                break;
            case Div:
                assembler.imm8(Opcode.DIV);
                break;
        }
    }
}
