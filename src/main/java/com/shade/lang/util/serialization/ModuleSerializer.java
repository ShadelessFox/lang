package com.shade.lang.util.serialization;

import com.shade.lang.runtime.objects.Chunk;
import com.shade.lang.runtime.objects.function.Guard;
import com.shade.lang.runtime.objects.module.Module;
import com.shade.lang.runtime.objects.value.NoneValue;
import com.shade.lang.util.Pair;
import com.shade.lang.util.annotations.NotNull;
import com.shade.lang.util.annotations.Nullable;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;

public class ModuleSerializer {
    // @formatter:off
    public static final int FILE_VERSION        = 2;
    public static final int FILE_SIGNATURE      = ('A' << 24) | ('S' << 16) | ('H' << 8) | (FILE_VERSION & 0xff);

    public static final byte CONSTANT_NONE      = 1;
    public static final byte CONSTANT_STRING    = 1 << 1;
    public static final byte CONSTANT_INTEGER   = 1 << 2;
    public static final byte CONSTANT_FLOAT     = 1 << 3;
    public static final byte CONSTANT_BOOL      = 1 << 4;
    public static final byte CONSTANT_CHUNK     = 1 << 5;
    // @formatter:on

    private ModuleSerializer() {
    }

    public static void writeModule(@NotNull DataOutputStream os, @NotNull Module module, int checksum) throws IOException {
        os.writeInt(FILE_SIGNATURE);
        os.writeInt(checksum);

        os.writeUTF(module.getName());
        os.writeUTF(module.getSource());

        if (module.getChunk() == null) {
            throw new IllegalArgumentException("Module has no chunk");
        }

        writeChunk(os, module.getChunk());
    }

    public static void writeChunk(@NotNull DataOutputStream os, @NotNull Chunk chunk) throws IOException {
        os.writeByte(chunk.getFlags());
        os.writeByte(chunk.getArguments());
        os.writeByte(chunk.getBoundArguments());
        os.writeByte(chunk.getLocals());

        os.writeInt(chunk.getCode().length);
        os.write(chunk.getCode());

        os.writeShort(chunk.getConstants().length);
        for (Object constant : chunk.getConstants()) {
            writeConstant(os, constant);
        }

        os.writeShort(chunk.getGuards().length);
        for (Guard guard : chunk.getGuards()) {
            os.writeInt(guard.getStart());
            os.writeInt(guard.getEnd());
            os.writeInt(guard.getOffset());
            os.writeByte(guard.getSlot());
        }

        os.writeShort(chunk.getLocations().size());
        for (Map.Entry<Integer, Pair<Short, Short>> location : chunk.getLocations().entrySet()) {
            os.writeInt(location.getKey());
            os.writeShort(location.getValue().getFirst());
            os.writeShort(location.getValue().getSecond());
        }
    }

    public static void writeConstant(@NotNull DataOutputStream os, @NotNull Object constant) throws IOException {
        if (constant instanceof NoneValue) {
            os.writeByte(CONSTANT_NONE);
        } else if (constant instanceof String) {
            os.writeByte(CONSTANT_STRING);
            os.writeUTF((String) constant);
        } else if (constant instanceof Integer) {
            os.writeByte(CONSTANT_INTEGER);
            os.writeInt((int) constant);
        } else if (constant instanceof Float) {
            os.writeByte(CONSTANT_FLOAT);
            os.writeFloat((float) constant);
        } else if (constant instanceof Boolean) {
            os.writeByte(CONSTANT_BOOL);
            os.writeBoolean((boolean) constant);
        } else if (constant instanceof Chunk) {
            os.writeByte(CONSTANT_CHUNK);
            writeChunk(os, (Chunk) constant);
        } else {
            throw new IllegalArgumentException("Unsupported constant: " + constant + "(" + constant.getClass() + ")");
        }
    }

    public static int readFileChecksum(@NotNull File file) throws IOException {
        try (FileInputStream is = new FileInputStream(file)) {
            final CRC32 crc32 = new CRC32();

            final byte[] buffer = new byte[8192];
            int offset = 0;
            int length;

            while ((length = (is.read(buffer, offset, buffer.length))) > 0) {
                crc32.update(buffer, offset, length);
            }

            return (int) crc32.getValue();
        }
    }

    @Nullable
    public static Module readModule(@NotNull DataInputStream is, int checksum) throws IOException {
        if (is.readInt() != FILE_SIGNATURE) {
            throw new IllegalArgumentException("Invalid file signature");
        }

        if (is.readInt() != checksum) {
            return null;
        }

        final String name = is.readUTF();
        final String source = is.readUTF();
        final Chunk chunk = readChunk(is);

        final Module module = new Module(name, source);
        module.setChunk(chunk);

        return module;
    }

    @NotNull
    public static Chunk readChunk(@NotNull DataInputStream is) throws IOException {
        final byte flags = is.readByte();
        final byte arguments = is.readByte();
        final byte boundArguments = is.readByte();
        final byte locals = is.readByte();

        final byte[] code = new byte[is.readInt()];
        if (is.read(code) != code.length) {
            throw new IllegalStateException("Chunk is truncated");
        }

        final Object[] constants = new Object[is.readShort()];
        for (int index = 0; index < constants.length; index++) {
            constants[index] = readConstant(is);
        }

        final Guard[] guards = new Guard[is.readShort()];
        for (int index = 0; index < guards.length; index++) {
            guards[index] = new Guard(is.readInt(), is.readInt(), is.readInt(), is.readByte());
        }

        final Map<Integer, Pair<Short, Short>> locations = new HashMap<>();
        for (int index = 0, length = is.readShort(); index < length; index++) {
            locations.put(is.readInt(), new Pair<>(is.readShort(), is.readShort()));
        }

        return new Chunk(
            code,
            constants,
            guards,
            flags,
            arguments,
            boundArguments,
            locals,
            locations
        );
    }

    @NotNull
    public static Object readConstant(@NotNull DataInputStream is) throws IOException {
        final byte type = is.readByte();

        switch (type) {
            case CONSTANT_NONE:
                return NoneValue.INSTANCE;
            case CONSTANT_STRING:
                return is.readUTF();
            case CONSTANT_INTEGER:
                return is.readInt();
            case CONSTANT_FLOAT:
                return is.readFloat();
            case CONSTANT_BOOL:
                return is.readBoolean();
            case CONSTANT_CHUNK:
                return readChunk(is);
            default:
                throw new IllegalStateException("Unknown constant type: " + type);
        }
    }
}
