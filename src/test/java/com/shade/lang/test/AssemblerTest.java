package com.shade.lang.test;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.compiler.OperationCode;
import com.shade.lang.parser.ScriptException;
import com.shade.lang.parser.node.Node;
import com.shade.lang.parser.node.context.Context;
import com.shade.lang.parser.node.expr.BinaryExpression;
import com.shade.lang.parser.node.expr.LoadConstantExpression;
import com.shade.lang.parser.node.expr.UnaryExpression;
import com.shade.lang.parser.node.stmt.*;
import com.shade.lang.parser.token.Region;
import com.shade.lang.parser.token.TokenKind;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;

public class AssemblerTest {
    private static final int MAX_CHUNK_SIZE = 1024;

    @Mock
    private Region region;

    @Mock
    private Context context;

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
        Mockito.when(region.getBegin()).thenReturn(new Region.Span(0, 0, 0));
        Mockito.when(region.getEnd()).thenReturn(new Region.Span(0, 0, 0));
        Mockito.when(context.addSlot(Mockito.any())).thenReturn(0x69);
    }

    @Test
    public void testLoadConstant() throws ScriptException {
        Assembler result = assemble(context, new LoadConstantExpression<>("abc", region));

        Assert.assertEquals(result.getConstants(), Collections.singletonList("abc"));
        Assert.assertArrayEquals(result.assemble().array(), new byte[]{
            OperationCode.OP_PUSH, 0x00, 0x00,
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
            OperationCode.OP_PUSH, 0x00, 0x00,
            OperationCode.OP_PUSH, 0x00, 0x01,
            OperationCode.OP_ADD
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
            OperationCode.OP_PUSH, 0x00, 0x00,
            OperationCode.OP_PUSH, 0x00, 0x01,
            OperationCode.OP_MUL
        });
    }

    @Test
    public void testReturn() throws ScriptException {
        ByteBuffer result = compile(new ReturnStatement(
            new LoadConstantExpression<>(1, region),
            region
        ));

        Assert.assertArrayEquals(result.array(), new byte[]{
            OperationCode.OP_PUSH, 0x00, 0x00,
            OperationCode.OP_RETURN
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
            OperationCode.OP_PUSH, 0x00, 0x00,
            OperationCode.OP_ASSERT, 0x00, 0x01, 0x00, 0x02
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
            OperationCode.OP_PUSH, 0x00, 0x00,
            OperationCode.OP_JUMP_IF_FALSE, 0x00, 0x07,
            OperationCode.OP_PUSH, 0x00, 0x01,
            OperationCode.OP_POP,
            OperationCode.OP_JUMP, 0x00, 0x04,
            OperationCode.OP_PUSH, 0x00, 0x02,
            OperationCode.OP_POP,
        });
    }

    @Test
    public void testLocalImport() throws ScriptException {
        Assembler result = assemble(context, new ImportStatement("abc", "def", false, region));

        Assert.assertEquals(result.getConstants(), Collections.singletonList("abc"));
        Assert.assertArrayEquals(result.assemble().array(), new byte[]{
            OperationCode.OP_IMPORT, 0x00, 0x00, 0x69
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
