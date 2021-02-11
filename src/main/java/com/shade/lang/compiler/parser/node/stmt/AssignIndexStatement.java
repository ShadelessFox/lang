package com.shade.lang.compiler.parser.node.stmt;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.assembler.Operation;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Expression;
import com.shade.lang.compiler.parser.node.Statement;
import com.shade.lang.compiler.parser.node.visitor.Visitor;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.util.annotations.NotNull;

public class AssignIndexStatement extends Statement {
    private final Expression target;
    private final Expression index;
    private final Expression value;

    public AssignIndexStatement(Expression target, Expression index, Expression value, Region region) {
        super(region);
        this.target = target;
        this.index = index;
        this.value = value;
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        target.compile(context, assembler);
        index.compile(context, assembler);
        value.compile(context, assembler);
        assembler.emit(Operation.SET_INDEX);
        assembler.addLocation(getRegion().getBegin());
    }

    @NotNull
    @Override
    public Statement accept(@NotNull Visitor visitor) {
        if (visitor.enterAssignIndexStatement(this)) {
            final Expression target = this.target.accept(visitor);
            final Expression index = this.index.accept(visitor);
            final Expression value = this.value.accept(visitor);

            if (target != this.target || index != this.index || value != this.value) {
                return visitor.leaveAssignIndexStatement(new AssignIndexStatement(target, index, value, getRegion()));
            } else {
                return visitor.leaveAssignIndexStatement(this);
            }
        }

        return this;
    }

    public Expression getTarget() {
        return target;
    }

    public Expression getIndex() {
        return index;
    }

    public Expression getValue() {
        return value;
    }
}
