package com.shade.lang.compiler.parser.node.stmt;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.assembler.Operand;
import com.shade.lang.compiler.assembler.Operation;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Expression;
import com.shade.lang.compiler.parser.node.Statement;
import com.shade.lang.compiler.parser.node.context.ClassContext;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.runtime.Machine;
import com.shade.lang.runtime.objects.Chunk;
import com.shade.lang.runtime.objects.function.Guard;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class DeclareClassStatement extends Statement {
    private static final Logger LOG = Logger.getLogger(DeclareClassStatement.class.getName());

    private final String name;
    private final List<Expression> bases;
    private final List<Statement> members;

    public DeclareClassStatement(String name, List<Expression> bases, List<Statement> members, Region region) {
        super(region);
        this.name = name;
        this.bases = Collections.unmodifiableList(bases);
        this.members = Collections.unmodifiableList(members);
    }

    @Override
    public void compile(Context context, Assembler assembler) throws ScriptException {
        final Assembler parentAssembler = assembler;

        assembler = new Assembler();
        assembler.addLocation(getRegion().getBegin());

        final ClassContext classContext = new ClassContext(context, name);
        final int classInstanceSlot = classContext.addSlot("<instance>");

        for (Statement member : members) {
            member.compile(classContext, assembler);
        }

        assembler.emit(Operation.GET_LOCAL, Operand.imm8(classInstanceSlot));
        assembler.emit(Operation.RETURN);

        final Chunk chunk = new Chunk(
            assembler.assemble().array(),
            assembler.getConstants().toArray(),
            new Guard[0],
            Chunk.FLAG_CLASS,
            (byte) 0,
            (byte) 0,
            (byte) 0,
            assembler.getComputedLocations()
        );

        for (Expression base : bases) {
            base.compile(context, parentAssembler);
        }

        parentAssembler.emit(Operation.MAKE_CLASS, Operand.constant(name), Operand.constant(chunk), Operand.imm8(bases.size()));
        parentAssembler.addLocation(getRegion().getBegin());
        parentAssembler.emit(Operation.SET_GLOBAL, Operand.constant(name));

        if (Machine.ENABLE_LOGGING) {
            StringWriter writer = new StringWriter();
            writer.write("Disassembly of class '" + name + "':\n");
            assembler.print(new PrintWriter(writer));
            LOG.info(writer.toString());
        }
    }

    public String getName() {
        return name;
    }

    public List<Expression> getBases() {
        return bases;
    }

    public List<Statement> getMembers() {
        return members;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeclareClassStatement that = (DeclareClassStatement) o;
        return name.equals(that.name) &&
            bases.equals(that.bases) &&
            members.equals(that.members);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, bases, members);
    }
}
