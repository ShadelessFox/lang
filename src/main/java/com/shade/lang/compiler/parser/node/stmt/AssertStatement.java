package com.shade.lang.compiler.parser.node.stmt;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.assembler.Operand;
import com.shade.lang.compiler.assembler.Operation;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Expression;
import com.shade.lang.compiler.parser.node.Statement;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.runtime.objects.value.NoneValue;
import com.shade.lang.util.annotations.NotNull;
import com.shade.lang.util.annotations.Nullable;

public class AssertStatement extends Statement {
    private final Expression condition;
    private final String source;
    private final String message;

    public AssertStatement(@NotNull Expression condition, @NotNull String source, @Nullable String message, @NotNull Region region) {
        super(region);
        this.condition = condition;
        this.message = message;
        this.source = source;
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        condition.compile(context, assembler);
        assembler.emit(Operation.ASSERT, Operand.constant(source), Operand.constant(message == null ? NoneValue.INSTANCE : message));
        assembler.addLocation(getRegion().getBegin());
    }

    public Expression getCondition() {
        return condition;
    }

    public String getSource() {
        return source;
    }

    public String getMessage() {
        return message;
    }
}
