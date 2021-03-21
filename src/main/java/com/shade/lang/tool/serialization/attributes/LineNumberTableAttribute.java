package com.shade.lang.tool.serialization.attributes;

import com.shade.lang.util.annotations.NotNull;
import com.shade.lang.util.annotations.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LineNumberTableAttribute implements Attribute<LineNumberTableAttribute> {
    public static final String ATTRIBUTE_NAME = "LineNumberTable";
    public static final Descriptor DESCRIPTOR = new Descriptor();

    private final Map<Integer, Location> locations;

    public LineNumberTableAttribute(@NotNull Map<Integer, Location> locations) {
        this.locations = locations;
    }

    public LineNumberTableAttribute() {
        this(new HashMap<>());
    }

    /**
     * Finds location information for specified address inside compiled code.
     *
     * @param position compiled code position
     * @return location that consists of <code>line-row</code> pair or <code>null</code> if not present
     */
    @Nullable
    public Location getLocationByAddress(int position) {
        return locations.get(position);
    }

    /**
     * Finds location information for specified line number inside source code.
     *
     * @param line line number inside source code
     * @return location that consists of <code>line-row</code> pair or <code>null</code> if not present
     */
    @Nullable
    public Location getLocationByLine(int line) {
        return locations.values().stream()
            .filter(location -> location.getLine() == line)
            .findFirst().orElse(null);
    }

    @NotNull
    @Override
    public AttributeDescriptor<LineNumberTableAttribute> getDescriptor() {
        return DESCRIPTOR;
    }

    public static class Location {
        private final short line;
        private final short column;

        public Location(short line, short column) {
            this.line = line;
            this.column = column;
        }

        public short getLine() {
            return line;
        }

        public short getColumn() {
            return column;
        }

        @Override
        public String toString() {
            return line + ":" + column;
        }
    }

    private static class Descriptor implements AttributeDescriptor<LineNumberTableAttribute> {
        @Override
        public LineNumberTableAttribute load(@NotNull DataInputStream is) throws IOException {
            final LineNumberTableAttribute instance = new LineNumberTableAttribute();
            for (int count = is.readInt(); count > 0; count--) {
                instance.locations.put(
                    is.readInt(),
                    new Location(is.readShort(), is.readShort())
                );
            }
            return instance;
        }

        @Override
        public void save(@NotNull DataOutputStream os, @NotNull LineNumberTableAttribute attribute) throws IOException {
            os.writeInt(attribute.locations.size());
            for (Map.Entry<Integer, Location> entry : attribute.locations.entrySet()) {
                os.writeInt(entry.getKey());
                os.writeShort(entry.getValue().getLine());
                os.writeShort(entry.getValue().getColumn());
            }
        }

        @NotNull
        @Override
        public String getName() {
            return ATTRIBUTE_NAME;
        }
    }
}
