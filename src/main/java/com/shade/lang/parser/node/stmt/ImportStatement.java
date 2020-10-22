package com.shade.lang.parser.node.stmt;

import com.shade.lang.parser.gen.Assembler;
import com.shade.lang.parser.gen.Opcode;
import com.shade.lang.parser.node.Visitor;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.token.Region;

public class ImportStatement implements Statement {
    private final String name;
    private final boolean path;
    private final Region region;

    public ImportStatement(String name, boolean path, Region region) {
        this.name = name;
        this.path = path;
        this.region = region;
    }

    public String getName() {
        return name;
    }

    public boolean isPath() {
        return path;
    }

    @Override
    public boolean isControlFlowReturned() {
        return false;
    }

    @Override
    public Region getRegion() {
        return region;
    }

    @Override
    public void emit(Context context, Assembler assembler) {
        assembler.span(region.getBegin());
        assembler.imm8(Opcode.IMPORT);
        assembler.imm32(assembler.constant(name));
        assembler.imm8(path);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
