package com.github.fallencrystal.lavender.api.accessor;

import com.github.fallencrystal.lavender.api.accessor.interfaces.InstanceCheck;
import lombok.SneakyThrows;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.NamingStrategy;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.Label;
import net.bytebuddy.matcher.ElementMatchers;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class AccessorFactory {
    static final @NotNull String PACKAGE = "com.github.fallencrystal.lavender.api.accessor.generated";
    private static final @NotNull Map<Class<?>, InstanceCheck> cachedInstanceChecks = new ConcurrentHashMap<>();
    private static final Set<Class<?>> primitiveTypes = Set.of(boolean.class, byte.class, short.class, int.class, long.class, double.class, float.class, char.class, void.class);

    private AccessorFactory() { throw new AssertionError(); }

    static boolean isPrimitive(final @NotNull Class<?> type) {
        return primitiveTypes.contains(type);
    }

    static int random() {
        return ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
    }



    public static @NotNull InstanceCheck getInstanceCheck(final @NotNull Class<?> type) throws IllegalAccessException {
        return getInstanceCheck(type.getClassLoader(), type);
    }


    public static @NotNull InstanceCheck getInstanceCheck(final @NotNull ClassLoader classLoader, final @NotNull Class<?> type) throws IllegalAccessException {
        if (type == Object.class) {
            throw new IllegalArgumentException("Cannot generate InstanceCheck for type Object.");
        }
        if (isPrimitive(type)) {
            throw new IllegalArgumentException("Cannot generate instanceof check for primitive types");
        }
        if (classLoader != InstanceCheck.class.getClassLoader()) {
            try {
                Class.forName(InstanceCheck.class.getName(), false, classLoader);
            } catch (final ClassNotFoundException e) {
                throw new IllegalAccessException("InstanceCheck class is not exists in target ClassLoader. " +
                        "Make sure InstanceCheck is exist in target ClassLoader. " +
                        "Or use InstanceCheck.class.getClassLoader() instead.");
            }
        }
        return cachedInstanceChecks.computeIfAbsent(type, k -> buildInstanceCheck(classLoader, k));
    }

    @SneakyThrows
    private static @NotNull InstanceCheck buildInstanceCheck(
            @NotNull ClassLoader classLoader,
            @NotNull Class<?> clazz) {
        return new ByteBuddy()
                .with(new NamingStrategy.AbstractBase() {
                    @Override
                    protected @NotNull String name(@NotNull TypeDescription typeDescription) {
                        return PACKAGE + "." + typeDescription.getSimpleName() + "$InstanceOfCheck$" + random();
                    }
                })
                .subclass(InstanceCheck.class)
                .annotateType(AnnotationsCollection.RUNTIME_GENERATED)
                .method(ElementMatchers.named("getTargetClass"))
                .intercept(FixedValue.value(clazz))
                .method(ElementMatchers.named("isInstance"))
                .intercept(getInstanceOfMethod(clazz))
                .make()
                .load(classLoader)
                .getLoaded()
                .getDeclaredConstructor()
                .newInstance();
    }

    static @NotNull Label[] createLabelArray(int size) {
        final Label[] labels = new Label[size];
        for (int i = 0; i < size; i++) labels[i] = new Label();
        return labels;
    }

    static @NotNull SimpleImplementation getInstanceOfMethod(final @NotNull Class<?> classToCheck) {
        return SimpleImplementation.of(((methodVisitor, context, methodDescription) -> {
            final @NotNull StackManipulation stackManipulation = new StackManipulation.Compound(
                    MethodVariableAccess.REFERENCE.loadFrom(1),
                    net.bytebuddy.implementation.bytecode.assign.InstanceCheck.of(TypeDescription.ForLoadedType.of(classToCheck)),
                    MethodReturn.INTEGER
            );
            final @NotNull StackManipulation.Size size = stackManipulation.apply(methodVisitor, context);
            return new ByteCodeAppender.Size(size.getMaximalSize(), methodDescription.getStackSize());
        }));
    }
    
}
