package com.shade.lang.tool.serialization.attributes;

import com.shade.lang.util.annotations.NotNull;
import com.shade.lang.util.annotations.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LocalVariableTableAttribute implements Attribute<LocalVariableTableAttribute> {
    public static final String ATTRIBUTE_NAME = "LocalVariableTable";
    public static final Descriptor DESCRIPTOR = new Descriptor();

    private final List<Variable> variables;

    public LocalVariableTableAttribute(@NotNull List<Variable> variables) {
        this.variables = variables;
    }

    public LocalVariableTableAttribute() {
        this(new ArrayList<>());
    }

    @Nullable
    public Variable getVariableByName(@NotNull String name) {
        return variables.stream()
            .filter(variable -> variable.getName().equals(name))
            .findFirst().orElse(null);
    }

    @Nullable
    public Variable getVariableByIndex(int index) {
        return variables.stream()
            .filter(variable -> variable.getIndex() == index)
            .findFirst().orElse(null);
    }

    @NotNull
    public List<Variable> getVariablesByPosition(int position) {
        return variables.stream()
            .filter(variable -> variable.within(position))
            .collect(Collectors.toList());
    }

    @NotNull
    @Override
    public AttributeDescriptor<LocalVariableTableAttribute> getDescriptor() {
        return DESCRIPTOR;
    }

    public static class Variable {
        private final int position;
        private final int length;
        private final int index;
        private final String name;

        public Variable(int position, int length, int index, String name) {
            this.position = position;
            this.length = length;
            this.index = index;
            this.name = name;
        }

        public boolean within(int position) {
            return this.position <= position && position < this.position + this.length;
        }

        /**
         * Returns the starting position of the span inside compiled code within which this variable has a value.
         * <p>
         * The value must point to a valid index of the opcode of an instruction inside code block.
         *
         * @return starting position of the span within which variable has a value
         */
        public int getPosition() {
            return position;
        }

        /**
         * Returns the length of the span inside compiled code within which this variable has a value.
         * <p>
         * The length creates an interval of <code>[position, position + length)</code>.
         *
         * @return length of the span within which variable has a value
         */
        public int getLength() {
            return length;
        }

        /**
         * Returns the index of local slot occupied inside call frame by this variable.
         *
         * @return local slot index of variable
         */
        public int getIndex() {
            return index;
        }

        /**
         * Returns the name of this variable used in source code.
         *
         * @return name of variable
         */
        public String getName() {
            return name;
        }
    }

    private static class Descriptor implements AttributeDescriptor<LocalVariableTableAttribute> {
        @Override
        public LocalVariableTableAttribute load(@NotNull DataInputStream is) throws IOException {
            final LocalVariableTableAttribute instance = new LocalVariableTableAttribute();
            for (int count = is.readInt(); count > 0; count--) {
                instance.variables.add(new Variable(
                    is.readInt(),
                    is.readShort(),
                    is.readShort(),
                    is.readUTF()
                ));
            }
            return instance;
        }

        @Override
        public void save(@NotNull DataOutputStream os, @NotNull LocalVariableTableAttribute attribute) throws IOException {
            os.writeInt(attribute.variables.size());
            for (Variable variable : attribute.variables) {
                os.writeInt(variable.getPosition());
                os.writeShort(variable.getLength());
                os.writeShort(variable.getIndex());
                os.writeUTF(variable.getName());
            }
        }

        @NotNull
        @Override
        public String getName() {
            return ATTRIBUTE_NAME;
        }
    }
}
