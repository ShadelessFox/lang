package com.shade.lang.runtime.objects.module;

import com.shade.lang.runtime.Machine;
import com.shade.lang.runtime.objects.Chunk;
import com.shade.lang.runtime.objects.ScriptObject;
import com.shade.lang.runtime.objects.function.NativeFunction;
import com.shade.lang.runtime.objects.module.builtin.BuiltinCore;
import com.shade.lang.runtime.objects.value.ArrayValue;
import com.shade.lang.runtime.objects.value.NoneValue;
import com.shade.lang.runtime.objects.value.Value;
import com.shade.lang.util.annotations.NotNull;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import sun.misc.Unsafe;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.*;

public abstract class NativeModule extends Module {
    public NativeModule(String moduleName) {
        super(moduleName, '<' + moduleName + '>');

        for (Method method : BuiltinCore.class.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(FunctionDescriptor.class)) {
                continue;
            }

            final FunctionDescriptor descriptor = method.getDeclaredAnnotation(FunctionDescriptor.class);
            final String name = descriptor.name().isEmpty() ? method.getName() : descriptor.name();

            final BridgeGenerator generator = new BridgeGenerator();
            final Class<?> bridge = generator.generate(method, descriptor);
            final NativeFunction.Prototype prototype;

            try {
                prototype = (NativeFunction.Prototype) bridge.getConstructor().newInstance();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }

            final NativeFunction function = new NativeFunction(
                this,
                name,
                (byte) (method.getParameterCount() - 1),
                method.isVarArgs() ? Chunk.FLAG_VARIADIC : 0,
                prototype
            );

            setAttribute(name, function);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    protected @interface FunctionDescriptor {
        String name() default "";

        String[] arguments() default {};
    }

    private static final class BridgeGenerator {
        private static final Unsafe UNSAFE = getUnsafe();

        public Class<?> generate(@NotNull Method method, @NotNull FunctionDescriptor descriptor) {
            if (method.getParameterCount() == 0 || method.getParameterTypes()[0] != Machine.class) {
                throw new IllegalArgumentException("Method should accept `Machine` as first argument");
            }

            if (method.getReturnType() != Object.class && method.getReturnType() != void.class) {
                throw new IllegalArgumentException("Method should return either `Object` or nothing");
            }

            final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

            cw.visit(
                V1_5,
                ACC_PUBLIC | ACC_FINAL | ACC_SYNTHETIC,
                Type.getInternalName(method.getDeclaringClass()) + '$' + method.getName(),
                null,
                Type.getInternalName(Object.class),
                new String[]{Type.getInternalName(NativeFunction.Prototype.class)}
            );

            {
                final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
                mv.visitCode();

                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
                mv.visitInsn(RETURN);

                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }

            {
                final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_VARARGS, "invoke", "(Lcom/shade/lang/runtime/Machine;[Lcom/shade/lang/runtime/objects/ScriptObject;)Ljava/lang/Object;", null, null);
                mv.visitCode();

                // Push machine instance
                mv.visitVarInsn(ALOAD, 1);

                // Push other arguments and cast, if required
                for (int index = 0; index < method.getParameterCount() - 1; index++) {
                    final String name = descriptor.arguments().length > index ? descriptor.arguments()[index] : method.getParameters()[index].getName();

                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitLdcInsn(index);
                    mv.visitInsn(AALOAD);
                    emitCast(mv, name, method.getParameterTypes()[index + 1]);
                }

                // Call native method
                mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(method.getDeclaringClass()), method.getName(), Type.getType(method).toString(), false);

                // If native method does not return any value, then push 'none' manually
                if (method.getReturnType() == void.class) {
                    mv.visitFieldInsn(GETSTATIC, Type.getInternalName(NoneValue.class), "INSTANCE", Type.getDescriptor(NoneValue.class));
                }

                mv.visitInsn(ARETURN);

                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }

            return UNSAFE.defineAnonymousClass(BridgeGenerator.class, cw.toByteArray(), null);
        }

        private static void emitCast(MethodVisitor visitor, String name, Class<?> type) {
            if (type == ScriptObject.class || type == Object.class) {
                return;
            }

            if (type.isPrimitive()) {
                emitPrimitiveCast(visitor, name, type);
            } else if (type.isArray()) {
                if (type.getComponentType() != ScriptObject.class && type.getComponentType() != Object.class) {
                    throw new IllegalArgumentException("Array must be of type `ScriptObject` or `Object`");
                }

                if (type.getComponentType().isPrimitive()) {
                    throw new IllegalArgumentException("Primitive arrays are not supported");
                }

                // Limitations are silly, but that's enough for now ...

                visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(ArrayValue.class));
                visitor.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(ArrayValue.class), "getValues", "()[Lcom/shade/lang/runtime/objects/ScriptObject;", false);
                visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(type));
            } else {
                visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(Value.class));
                visitor.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Value.class), "getValue", "()Ljava/lang/Object;", false);
                visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(type));
            }
        }

        private static void emitPrimitiveCast(MethodVisitor visitor, String name, Class<?> type) {
            visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(Value.class));
            visitor.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Value.class), "getValue", "()Ljava/lang/Object;", false);

            if (type == int.class) {
                emitPrimitiveUnbox(visitor, name, type, Integer.class, "intValue");
            } else if (type == float.class) {
                emitPrimitiveUnbox(visitor, name, type, Float.class, "floatValue");
            } else if (type == boolean.class) {
                emitPrimitiveUnbox(visitor, name, type, Boolean.class, "booleanValue");
            } else {
                throw new IllegalArgumentException("Unsupported primitive type: " + type);
            }
        }

        private static void emitPrimitiveUnbox(MethodVisitor visitor, String name, Class<?> type, Class<?> boxedType, String boxedTypeGetter) {
            final Label label = new Label();

            visitor.visitInsn(DUP);
            visitor.visitTypeInsn(INSTANCEOF, Type.getInternalName(boxedType));
            visitor.visitJumpInsn(IFNE, label);

            {
                // new StringBuilder()
                visitor.visitTypeInsn(NEW, Type.getInternalName(StringBuilder.class));
                visitor.visitInsn(DUP);
                visitor.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(StringBuilder.class), "<init>", "()V", false);

                // .append(...)
                visitor.visitLdcInsn("Parameter '" + name + "' of type " + type + " is incompatible with value ");
                visitor.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(StringBuilder.class), "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);

                // .append(...)
                visitor.visitInsn(SWAP);
                visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(ScriptObject.class));
                visitor.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(ScriptObject.class), "toDisplayString", "()Ljava/lang/String;", false);
                visitor.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(StringBuilder.class), "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false);

                // .toString()
                visitor.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(StringBuilder.class), "toString", "()Ljava/lang/String;", false);

                // machine.panic(...)
                visitor.visitVarInsn(ALOAD, 1);
                visitor.visitInsn(SWAP);
                visitor.visitIntInsn(BIPUSH, 1);
                visitor.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Machine.class), "panic", "(Ljava/lang/Object;Z)V", false);

                // return null
                visitor.visitInsn(ACONST_NULL);
                visitor.visitInsn(ARETURN);
            }

            visitor.visitLabel(label);

            {
                visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(boxedType));
                visitor.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(boxedType), boxedTypeGetter, "()" + Type.getDescriptor(type), false);
            }
        }

        @NotNull
        private static Unsafe getUnsafe() {
            try {
                final Field instanceField = Unsafe.class.getDeclaredField("theUnsafe");
                instanceField.setAccessible(true);
                return (Unsafe) instanceField.get(null);
            } catch (Exception e) {
                throw new IllegalStateException("Cannot obtain sun.misc.Unsafe", e);
            }
        }
    }
}
