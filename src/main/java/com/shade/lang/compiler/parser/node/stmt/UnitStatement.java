package com.shade.lang.compiler.parser.node.stmt;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.assembler.Operand;
import com.shade.lang.compiler.assembler.Operation;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Statement;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.runtime.Machine;
import com.shade.lang.runtime.objects.Chunk;
import com.shade.lang.runtime.objects.function.Guard;
import com.shade.lang.runtime.objects.value.NoneValue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class UnitStatement extends Statement {
    private static final Logger LOG = Logger.getLogger(UnitStatement.class.getName());

    private final String name;
    private final List<Statement> statements;

    public UnitStatement(String name, List<Statement> statements, Region region) {
        super(region);
        this.name = name;
        this.statements = Collections.unmodifiableList(statements);
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        assembler = new Assembler();
        assembler.addLocation(getRegion().getBegin());

        for (Statement statement : statements) {
            statement.compile(context, assembler);
        }

        assembler.emit(Operation.PUSH, Operand.constant(NoneValue.INSTANCE));
        assembler.emit(Operation.RETURN);

        final Chunk chunk = new Chunk(
            assembler.assemble().array(),
            assembler.getConstants().toArray(),
            new Guard[0],
            Chunk.FLAG_MODULE,
            (byte) 0,
            (byte) 0,
            (byte) 0,
            assembler.getComputedLocations()
        );

        context.getModule().setChunk(chunk);

        if (Machine.ENABLE_LOGGING) {
            StringWriter writer = new StringWriter();
            writer.write("Disassembly of module '" + name + "':\n");
            assembler.print(new PrintWriter(writer));
            LOG.info(writer.toString());
        }
    }

    public String getName() {
        return name;
    }

    public List<Statement> getStatements() {
        return statements;
    }
}
