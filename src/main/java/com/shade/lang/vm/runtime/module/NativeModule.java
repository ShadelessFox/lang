package com.shade.lang.vm.runtime.module;

import com.shade.lang.vm.Machine;
import com.shade.lang.vm.runtime.ScriptObject;
import com.shade.lang.vm.runtime.function.Function;
import com.shade.lang.vm.runtime.function.NativeFunction;
import com.shade.lang.vm.runtime.module.builtin.BuiltinCore;
import com.shade.lang.vm.runtime.value.ArrayValue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiFunction;

public abstract class NativeModule extends Module {
    public NativeModule(String moduleName) {
        super(moduleName, '<' + moduleName + '>');

        for (Method method : BuiltinCore.class.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(BuiltinCore.FunctionHandler.class)) {
                continue;
            }

            if (method.getParameterCount() == 0 || method.getParameterTypes()[0] != Machine.class) {
                throw new IllegalArgumentException("Method annotated with @FunctionHandler must have at least one parameter of type Machine");
            }

            BuiltinCore.FunctionHandler handler = method.getDeclaredAnnotation(BuiltinCore.FunctionHandler.class);
            String name = handler.name().isEmpty() ? method.getName() : handler.name();

            NativeFunction function = new NativeFunction(
                this,
                name,
                method.getParameterCount() - 1,
                method.isVarArgs() ? Function.FLAG_VARIADIC : 0,
                wrap(method)
            );

            setAttribute(name, function);
        }
    }

    protected static BiFunction<Machine, ScriptObject[], Object> wrap(Method method) {
        return (machine, args) -> {
            try {
                Object[] objects = new Object[args.length + 1];
                objects[0] = machine;
                System.arraycopy(args, 0, objects, 1, args.length);
                if (method.isVarArgs()) {
                    objects[args.length] = ((ArrayValue) objects[args.length]).getValues();
                }
                return method.invoke(null, objects);
            } catch (IllegalAccessException | InvocationTargetException e) {
                machine.panic("Internal error: " + e);
                return null;
            }
        };
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    protected @interface FunctionHandler {
        String name() default "";
    }
}
