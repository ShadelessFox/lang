package com.shade.lang.tool.serialization;

import com.shade.lang.compiler.assembler.Disassembler;
import com.shade.lang.compiler.assembler.Instruction;
import com.shade.lang.compiler.assembler.Verifier;
import com.shade.lang.runtime.objects.Chunk;
import com.shade.lang.runtime.objects.function.Guard;
import com.shade.lang.runtime.objects.module.Module;
import com.shade.lang.runtime.objects.value.NoneValue;
import com.shade.lang.tool.serialization.attributes.*;
import com.shade.lang.util.annotations.NotNull;
import com.shade.lang.util.annotations.Nullable;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.Logger;
import java.util.zip.CRC32;

public class ModuleSerializer {
    // @formatter:off
    public static final int FILE_VERSION        = 3;
    public static final int FILE_SIGNATURE      = ('A' << 24) | ('S' << 16) | ('H' << 8) | (FILE_VERSION & 0xff);

    public static final byte CONSTANT_NONE      = 1;
    public static final byte CONSTANT_STRING    = 1 << 1;
    public static final byte CONSTANT_INTEGER   = 1 << 2;
    public static final byte CONSTANT_FLOAT     = 1 << 3;
    public static final byte CONSTANT_BOOL      = 1 << 4;
    public static final byte CONSTANT_CHUNK     = 1 << 5;
    // @formatter:on

    private static final Logger LOG = Logger.getLogger(ModuleSerializer.class.getName());
    private static Map<String, AttributeDescriptor<?>> availableAttributeDescriptors;

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

        writeAttributes(os, chunk.getFlattenAttributes());
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void writeAttributes(@NotNull DataOutputStream os, @NotNull Attribute<?>[] attributes) throws IOException {
        os.writeShort(attributes.length);
        for (Attribute<?> attribute : attributes) {
            // Abuse type erasure to make this work...
            ModuleSerializer.<Attribute>writeAttribute(os, attribute);
        }
    }

    public static <T extends Attribute<T>> void writeAttribute(@NotNull DataOutputStream os, @NotNull T attribute) throws IOException {
        final AttributeDescriptor<T> descriptor = attribute.getDescriptor();
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (final DataOutputStream stream = new DataOutputStream(buffer)) {
            descriptor.save(stream, attribute);
        }
        os.writeUTF(descriptor.getName());
        os.writeInt(buffer.size());
        os.write(buffer.toByteArray());
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

        final List<Attribute<?>> attributes = new ArrayList<>();
        for (int count = is.readShort(); count > 0; count--) {
            final String name = is.readUTF();
            final int length = is.readInt();
            final AttributeDescriptor<?> descriptor = availableAttributeDescriptors().get(name);

            if (descriptor == null) {
                LOG.severe(() -> "Cannot find descriptor for attribute '" + name + "', skipping");
                is.skipBytes(length);
                continue;
            }

            final byte[] buffer = new byte[length];
            is.readFully(buffer);

            try (DataInputStream stream = new DataInputStream(new ByteArrayInputStream(buffer))) {
                attributes.add(descriptor.load(stream));
            }
        }

        final Disassembler disassembler = new Disassembler(
            ByteBuffer.wrap(code),
            index -> index >= 0 && index < constants.length ? constants[index] : null
        );

        try {
            final List<Instruction> instructions = new ArrayList<>();

            while (true) {
                final Optional<Instruction> info = disassembler.next();
                if (!info.isPresent()) {
                    break;
                }
                instructions.add(info.get());
            }

            final Verifier verifier = new Verifier(instructions.toArray(new Instruction[0]));
            verifier.verify();
        } catch (Disassembler.DisassemblerException e) {
            throw new IOException("Cannot disassemble code", e);
        } catch (Verifier.VerificationException e) {
            throw new IOException("Cannot verify code", e);
        }

        return new Chunk(
            code,
            constants,
            guards,
            flags,
            arguments,
            boundArguments,
            locals,
            attributes.toArray(new Attribute[0])
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

    @NotNull
    public static Map<String, AttributeDescriptor<?>> availableAttributeDescriptors() {
        if (availableAttributeDescriptors == null) {
            availableAttributeDescriptors = new HashMap<>();
            for (AttributeProvider provider : ServiceLoader.load(AttributeProvider.class)) {
                final Iterator<AttributeDescriptor<?>> iterator = provider.attributes();
                while (iterator.hasNext()) {
                    final AttributeDescriptor<?> descriptor = iterator.next();
                    final String name = descriptor.getName();
                    if (availableAttributeDescriptors.containsKey(name)) {
                        LOG.severe(() -> "Duplicated descriptor for attribute '" + name + "': " + descriptor.getClass().getName());
                        continue;
                    }
                    availableAttributeDescriptors.put(name, descriptor);
                }
            }
        }
        return Collections.unmodifiableMap(availableAttributeDescriptors);
    }

    public static class DebugAttributeProvider implements AttributeProvider {
        private final List<AttributeDescriptor<?>> SUPPORTED_ATTRIBUTES = Arrays.asList(
            LineNumberTableAttribute.DESCRIPTOR,
            LocalVariableTableAttribute.DESCRIPTOR
        );

        @NotNull
        @Override
        public Iterator<AttributeDescriptor<?>> attributes() {
            return SUPPORTED_ATTRIBUTES.iterator();
        }

        @Nullable
        @Override
        public AttributeDescriptor<?> attributeForName(@NotNull String name) {
            return SUPPORTED_ATTRIBUTES.stream()
                .filter(descriptor -> descriptor.getName().equals(name))
                .findFirst().orElse(null);
        }
    }
}
