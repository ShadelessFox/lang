package com.shade.lang.test;

import com.shade.lang.parser.Parser;
import com.shade.lang.parser.ScriptException;
import com.shade.lang.parser.Tokenizer;
import com.shade.lang.parser.node.Node;
import com.shade.lang.parser.node.expr.*;
import com.shade.lang.parser.token.Region;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;

public class ParserTest {
    @Mock
    private Region region;

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
        Mockito.when(region.getBegin()).thenReturn(new Region.Span(0, 0, 0));
        Mockito.when(region.getEnd()).thenReturn(new Region.Span(0, 0, 0));
    }

    @Test
    public void testChainedAttribute() throws ScriptException {
        Node result = prepare("a.b.c").parseExpression();
        Assert.assertEquals(result, new LoadAttributeExpression(
            new LoadAttributeExpression(
                new LoadSymbolExpression(
                    "a",
                    region
                ),
                "b",
                region
            ),
            "c",
            region
        ));
    }

    @Test
    public void testChainedCall() throws ScriptException {
        Node result = prepare("a()()").parseExpression();
        Assert.assertEquals(result, new CallExpression(
            new CallExpression(
                new LoadSymbolExpression(
                    "a",
                    region
                ),
                Collections.emptyList(),
                region
            ),
            Collections.emptyList(),
            region
        ));
    }

    @Test
    public void testCompound() throws ScriptException {
        Node result = prepare("(a)").parseExpression();
        Assert.assertEquals(result, new CompoundExpression(
            new LoadSymbolExpression("a", region),
            region
        ));
    }

    @Test
    public void testCallMultiple() throws ScriptException {
        Node result = prepare("a(b, c, d)").parseExpression();
        Assert.assertEquals(result, new CallExpression(
            new LoadSymbolExpression(
                "a",
                region
            ),
            Arrays.asList(
                new LoadSymbolExpression("b", region),
                new LoadSymbolExpression("c", region),
                new LoadSymbolExpression("d", region)
            ),
            region
        ));
    }

    @Test
    public void testChainedMix() throws ScriptException {
        Node result = prepare("a((b)).a(a, b, c().a)").parseExpression();
        Assert.assertEquals(result, new CallExpression(
            new LoadAttributeExpression(
                new CallExpression(
                    new LoadSymbolExpression("a", region),
                    Collections.singletonList(
                        new CompoundExpression(new LoadSymbolExpression("b", region), region)
                    ),
                    region
                ),
                "a",
                region
            ),
            Arrays.asList(
                new LoadSymbolExpression("a", region),
                new LoadSymbolExpression("b", region),
                new LoadAttributeExpression(
                    new CallExpression(
                        new LoadSymbolExpression("c", region),
                        Collections.emptyList(),
                        region
                    ),
                    "a",
                    region
                )
            ),
            region
        ));
    }

    @Test
    public void testIndex() throws ScriptException {
        Node result = prepare("a[b[c]][d]").parseExpression();
        Assert.assertEquals(
            new LoadIndexExpression(
                new LoadIndexExpression(
                    new LoadSymbolExpression("a", region),
                    new LoadIndexExpression(
                        new LoadSymbolExpression("b", region),
                        new LoadSymbolExpression("c", region),
                        region
                    ),
                    region
                ),
                new LoadSymbolExpression("d", region),
                region
            ),
            result
        );
    }

    private Parser prepare(String source) throws ScriptException {
        return new Parser(new Tokenizer(new StringReader(source)));
    }
}
