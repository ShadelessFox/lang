package com.shade.lang.parser.node.expr;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.gen.Opcode;
import com.shade.lang.parser.node.Visitor;
import com.shade.lang.parser.token.TokenKind;
import com.shade.lang.vm.runtime.Module;

public class UnaryExpression implements Expression {
    private final Expression rhs;
    private final TokenKind operator;

    public UnaryExpression(Expression rhs, TokenKind operator) {
        this.rhs = rhs;
        this.operator = operator;
    }

    public Expression getRhs() {
        return rhs;
    }

    public TokenKind getOperator() {
        return operator;
    }

    @Override
    public void emit(Module module, Assembler assembler) {
        rhs.emit(module, assembler);

        switch (operator) {
            case Not:
                assembler.imm8(Opcode.NOT);
                break;
        }
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
