package com.shade.lang.test;

import com.shade.lang.compiler.assembler.Assembler;
import com.shade.lang.compiler.assembler.Operand;
import com.shade.lang.compiler.parser.ScriptException;
import com.shade.lang.compiler.parser.node.Node;
import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.compiler.parser.node.expr.BinaryExpression;
import com.shade.lang.compiler.parser.node.expr.LoadConstantExpression;
import com.shade.lang.compiler.parser.node.expr.UnaryExpression;
import com.shade.lang.compiler.parser.node.stmt.*;
import com.shade.lang.compiler.parser.token.Region;
import com.shade.lang.compiler.parser.token.TokenKind;
import com.shade.lang.runtime.objects.module.Module;
import org.junit.Assert;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;

import static com.shade.lang.compiler.assembler.OperationCode.*;

public class AssemblerTest {
    private final Region region = new Region(new Region.Span(0, 0, 0), new Region.Span(0, 0, 0));
    private final Context context = new Context((Module) null);

    @Test
    public void testLoadConstant() throws ScriptException {
        Assembler result = assemble(context, new LoadConstantExpression<>("abc", region));

        Assert.assertEquals(result.getConstants(), Collections.singletonList("abc"));
        Assert.assertArrayEquals(result.assemble().array(), new byte[]{
            OP_PUSH, 0x00, 0x00,
        });
    }

    @Test
    public void testBinOp() throws ScriptException {
        ByteBuffer result = compile(new BinaryExpression(
            new LoadConstantExpression<>(1, region),
            new LoadConstantExpression<>(2, region),
            TokenKind.Add,
            region
        ));

        Assert.assertArrayEquals(result.array(), new byte[]{
            OP_PUSH, 0x00, 0x00,
            OP_PUSH, 0x00, 0x01,
            OP_ADD
        });
    }

    @Test
    public void testUnaryOp() throws ScriptException {
        ByteBuffer result = compile(new UnaryExpression(
            new LoadConstantExpression<>(1, region),
            TokenKind.Sub,
            region
        ));

        Assert.assertArrayEquals(result.array(), new byte[]{
            OP_PUSH, 0x00, 0x00,
            OP_PUSH, 0x00, 0x01,
            OP_SUB
        });
    }

    @Test
    public void testReturn() throws ScriptException {
        ByteBuffer result = compile(new ReturnStatement(
            new LoadConstantExpression<>(1, region),
            region
        ));

        Assert.assertArrayEquals(result.array(), new byte[]{
            OP_PUSH, 0x00, 0x00,
            OP_RETURN
        });
    }

    @Test
    public void testAssert() throws ScriptException {
        Assembler result = assemble(context, new AssertStatement(
            new LoadConstantExpression<>(1, region),
            "abc",
            "def",
            region
        ));

        Assert.assertEquals(result.getConstants(), Arrays.asList(1, "abc", "def"));
        Assert.assertArrayEquals(result.assemble().array(), new byte[]{
            OP_PUSH, 0x00, 0x00,
            OP_ASSERT, 0x00, 0x01, 0x00, 0x02
        });
    }

    @Test
    public void testBranch() throws ScriptException {
        ByteBuffer result = compile(new BranchStatement(
            new LoadConstantExpression<>(1, region),
            new ExpressionStatement(new LoadConstantExpression<>(2, region), region),
            new ExpressionStatement(new LoadConstantExpression<>(3, region), region),
            region
        ));

        Assert.assertArrayEquals(result.array(), new byte[]{
            OP_PUSH, 0x00, 0x00,
            OP_JUMP_IF_FALSE, 0x00, 0x07,
            OP_PUSH, 0x00, 0x01,
            OP_POP,
            OP_JUMP, 0x00, 0x04,
            OP_PUSH, 0x00, 0x02,
            OP_POP,
        });
    }

    @Test
    public void testLocalImport() throws ScriptException {
        Assembler result = assemble(context, new ImportStatement("abc", "def", false, region));
        Assert.assertEquals(result.getConstants(), Collections.singletonList("abc"));
        Assert.assertArrayEquals(result.assemble().array(), new byte[]{
            OP_IMPORT, 0x00, 0x00, 0x00
        });
    }

    @Test
    public void testGlobalImport() throws ScriptException {
        Assembler result = assemble(context, new ImportStatement("abc", "def", true, region));
        Assert.assertEquals(result.getConstants(), Arrays.asList("abc", "def"));
        Assert.assertArrayEquals(result.assemble().array(), new byte[]{
            OP_IMPORT, 0x00, 0x00, Operand.UNDEFINED,
            OP_SET_GLOBAL, 0x00, 0x01
        });
    }

    private Assembler assemble(Context context, Node node) throws ScriptException {
        Assembler assembler = new Assembler();
        node.compile(context, assembler);
        StringWriter writer = new StringWriter();
        assembler.print(new PrintWriter(writer));
        System.out.println(writer);
        return assembler;
    }

    private ByteBuffer compile(Node node) throws ScriptException {
        return assemble(context, node).assemble();
    }
}
