package com.shade.lang.compiler.parser.node.stmt;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.assembler.Operation;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Expression;
import com.shade.lang.compiler.parser.node.Statement;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.util.annotations.NotNull;

public class ThrowStatement extends Statement {
    private final Expression value;

    public ThrowStatement(@NotNull Expression value, @NotNull Region region) {
        super(region);
        this.value = value;
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        value.compile(context, assembler);
        assembler.emit(Operation.THROW);
        assembler.addLocation(getRegion().getBegin());
    }

    @NotNull
    public Expression getValue() {
        return value;
    }
}
