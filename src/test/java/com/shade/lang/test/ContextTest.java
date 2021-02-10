package com.shade.lang.test;

import com.shade.lang.compiler.parser.node.context.Context;
import com.shade.lang.runtime.objects.module.Module;
import org.junit.Assert;
import org.junit.Test;

public class ContextTest {
    @Test
    public void testLocals() {
        Context context1 = new Context((Module) null);
        {
            Assert.assertEquals(0, context1.addSlot("a"));
            Assert.assertEquals(1, context1.addSlot("b"));
            Assert.assertEquals(2, context1.addSlot("c"));

            Context context2 = new Context(context1);
            {
                try (Context inner = context2.enter()) {
                    Assert.assertEquals(0, inner.addSlot("a"));
                    Assert.assertEquals(1, inner.addSlot("b"));
                    Assert.assertEquals(2, inner.addSlot("c"));
                }

                try (Context inner = context2.enter()) {
                    Assert.assertEquals(3, inner.addSlot("d"));
                    Assert.assertEquals(4, inner.addSlot("e"));
                    Assert.assertEquals(5, inner.addSlot("f"));

                    try (Context reallyDeep = context2.enter()) {
                        Assert.assertEquals(6, reallyDeep.addSlot("g"));
                        Assert.assertEquals(7, reallyDeep.addSlot("h"));
                        Assert.assertEquals(8, reallyDeep.addSlot("i"));
                    }
                }

                Assert.assertEquals(3, context2.addSlot("d"));
                Assert.assertEquals(4, context2.addSlot("e"));
                Assert.assertEquals(5, context2.addSlot("f"));

                Assert.assertTrue(context2.hasSlot("a"));
                Assert.assertTrue(context2.hasSlot("b"));
                Assert.assertTrue(context2.hasSlot("c"));
                Assert.assertTrue(context2.hasSlot("d"));
                Assert.assertTrue(context2.hasSlot("e"));
                Assert.assertTrue(context2.hasSlot("f"));
                Assert.assertFalse(context2.hasSlot("g"));
                Assert.assertFalse(context2.hasSlot("h"));
                Assert.assertFalse(context2.hasSlot("i"));
            }
        }

        Context context3 = new Context(context1);
        {
            Assert.assertEquals(3, context3.addSlot("g"));
            Assert.assertEquals(4, context3.addSlot("h"));
            Assert.assertEquals(5, context3.addSlot("i"));
        }

        Assert.assertTrue(context1.hasSlot("a"));
        Assert.assertTrue(context1.hasSlot("b"));
        Assert.assertTrue(context1.hasSlot("c"));
    }
}
