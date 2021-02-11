package com.shade.lang.compiler.parser.node.stmt;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.assembler.Operand;
import com.shade.lang.compiler.assembler.Operation;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Expression;
import com.shade.lang.compiler.parser.node.Statement;
import com.shade.lang.compiler.parser.node.visitor.Visitor;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.util.annotations.NotNull;

public class AssignAttributeStatement extends Statement {
    private final Expression target;
    private final String name;
    private final Expression value;

    public AssignAttributeStatement(Expression target, String name, Expression value, Region region) {
        super(region);
        this.target = target;
        this.name = name;
        this.value = value;
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        target.compile(context, assembler);
        value.compile(context, assembler);
        assembler.emit(Operation.SET_ATTRIBUTE, Operand.constant(name));
        assembler.addLocation(getRegion().getBegin());
    }

    @NotNull
    @Override
    public Statement accept(@NotNull Visitor visitor) {
        if (visitor.enterAssignAttributeStatement(this)) {
            final Expression target = this.target.accept(visitor);
            final Expression value = this.value.accept(visitor);

            if (target != this.target || value != this.value) {
                return visitor.leaveAssignAttributeStatement(new AssignAttributeStatement(target, name, value, getRegion()));
            } else {
                return visitor.leaveAssignAttributeStatement(this);
            }
        }

        return this;
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
}
