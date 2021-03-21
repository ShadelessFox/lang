package com.shade.lang.tool.serialization.attributes;

import com.shade.lang.util.annotations.NotNull;
import com.shade.lang.util.annotations.Nullable;

import java.util.Iterator;

public interface AttributeProvider {
    /**
     * Creates an iterator that iterates over the
     * attribute descriptors supported by this provider.
     *
     * @return the new iterator
     */
    @NotNull
    Iterator<AttributeDescriptor<?>> attributes();

    /**
     * Retrieves an iterator descriptor for the given name.
     *
     * @param name name of an attribute
     * @return an descriptor of the named attribute, or <code>null</code> if the
     * named attribute is not supported by this provider.
     */
    @Nullable
    AttributeDescriptor<?> attributeForName(@NotNull String name);
}
