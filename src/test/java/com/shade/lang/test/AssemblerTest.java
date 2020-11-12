package com.shade.lang.test;

import com.shade.lang.compiler.Assembler;
import com.shade.lang.compiler.Opcode;
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

import java.nio.ByteBuffer;

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

        Assert.assertArrayEquals(result.getConstants(), new String[]{ "abc" });
        Assert.assertArrayEquals(result.build().array(), new byte[]{
            Opcode.PUSH_CONST, 0x00, 0x00, 0x00, 0x00,
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
            Opcode.PUSH_CONST, 0x00, 0x00, 0x00, 0x00,
            Opcode.PUSH_CONST, 0x00, 0x00, 0x00, 0x01,
            Opcode.ADD
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
            Opcode.PUSH_CONST, 0x00, 0x00, 0x00, 0x00,
            Opcode.PUSH_CONST, 0x00, 0x00, 0x00, 0x01,
            Opcode.MUL
        });
    }

    @Test
    public void testReturn() throws ScriptException {
        ByteBuffer result = compile(new ReturnStatement(
            new LoadConstantExpression<>(1, region),
            region
        ));

        Assert.assertArrayEquals(result.array(), new byte[]{
            Opcode.PUSH_CONST, 0x00, 0x00, 0x00, 0x00,
            Opcode.RET
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

        Assert.assertArrayEquals(result.getConstants(), new Object[]{ 1, "abc", "def" });
        Assert.assertArrayEquals(result.build().array(), new byte[]{
            Opcode.PUSH_CONST, 0x00, 0x00, 0x00, 0x00,
            Opcode.ASSERT, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x02
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
            Opcode.PUSH_CONST, 0x00, 0x00, 0x00, 0x00,
            Opcode.JUMP_IF_FALSE, 0x00, 0x00, 0x00, 0x0F,
            Opcode.PUSH_CONST, 0x00, 0x00, 0x00, 0x01,
            Opcode.POP,
            Opcode.JUMP, 0x00, 0x00, 0x00, 0x0A,
            Opcode.PUSH_CONST, 0x00, 0x00, 0x00, 0x02,
            Opcode.POP,
        });
    }

    @Test
    public void testLocalImport() throws ScriptException {
        Assembler result = assemble(context, new ImportStatement("abc", "def", false, region));

        Assert.assertArrayEquals(result.getConstants(), new String[]{ "abc" });
        Assert.assertArrayEquals(result.build().array(), new byte[]{
            Opcode.IMPORT, 0x00, 0x00, 0x00, 0x00, 0x69
        });
    }

    private Assembler assemble(Context context, Node node) throws ScriptException {
        Assembler assembler = new Assembler(MAX_CHUNK_SIZE);
        node.compile(context, assembler);
        assembler.dump(System.out);
        return assembler;
    }

    private ByteBuffer compile(Node node) throws ScriptException {
        return assemble(context, node).build();
    }
}
