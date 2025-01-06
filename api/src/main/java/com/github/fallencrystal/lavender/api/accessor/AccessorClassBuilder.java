package com.github.fallencrystal.lavender.api.accessor;

import com.github.fallencrystal.lavender.api.accessor.annotation.Accessor;
import com.github.fallencrystal.lavender.api.accessor.interfaces.IAccessor;
import com.github.fallencrystal.lavender.api.pair.Pair;
import lombok.SneakyThrows;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.NamingStrategy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.jar.asm.Label;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

class AccessorClassBuilder<T extends IAccessor> {

    private static final @NotNull Method ACCESSOR_OBJECT_GETTER;
    private static final @NotNull Method ACCESSOR_TARGET_CLASS_GETTER;

    static {
        final Method[] methods = IAccessor.class.getDeclaredMethods();
        ACCESSOR_OBJECT_GETTER = methods[0];
        ACCESSOR_TARGET_CLASS_GETTER = methods[1];
    }

    private final @NotNull Class<T> accessor;
    private final @NotNull Class<?> targetClass;
    private final @NotNull Method[] targetMethods, targetDeclaredMethods;
    private final @NotNull Field[] targetFields, targetDeclaredFields;

    protected AccessorClassBuilder(@NotNull Class<T> accessor) {
        this.accessor = accessor;
        final Accessor annotation = accessor.getAnnotation(Accessor.class);
        if (annotation == null) throw new IllegalArgumentException("Accessor is missing @Accessor annotation");
        if (annotation.value() == Object.class) {
            if (annotation.className().isEmpty()) throw new IllegalArgumentException("Accessor is missing target. Please specify target class or class name.");
            try {
                this.targetClass = Class.forName(annotation.className(), true, accessor.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Cannot found target class " + annotation.className() + ". Make sure accessor's ClassLoader can access target class.", e);
            }
        } else {
            this.targetClass = annotation.value();
        }
        this.targetMethods = targetClass.getMethods();
        this.targetFields = targetClass.getDeclaredFields();
        this.targetDeclaredMethods = targetClass.getDeclaredMethods();
        this.targetDeclaredFields = targetClass.getDeclaredFields();
    }

    @SneakyThrows
    protected Function<Object, T> generate() {
        var definition = new ByteBuddy()
                .with(new NamingStrategy.AbstractBase() {
                    @Override
                    protected @NotNull String name(@NotNull TypeDescription typeDescription) {
                        return AccessorFactory.PACKAGE + "." + typeDescription.getSimpleName() + "$Accessor$" + AccessorFactory.random();
                    }
                })
                .subclass(accessor)
                .annotateType(AnnotationsCollection.RUNTIME_GENERATED)
                .annotateType(AnnotationsCollection.API_EXPERIMENTAL)
                .defineField("targetInstance", targetClass, Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL)
                .annotateField(AnnotationsCollection.NOT_NULL)
                .defineConstructor(Visibility.PUBLIC)
                .withParameters(targetClass)
                .intercept(MethodCall.invoke(Object.class.getMethod("requireNonNull", Object.class, String.class))
                        .withArgument(0)
                        .with("Target instance cannot be null")
                        .andThen(FieldAccessor.ofField("targetInstance").setsArgumentAt(0))
                )
                .defineConstructor(Visibility.PUBLIC)
                .withParameters(Object.class)
                .intercept(SimpleImplementation.of(((methodVisitor, context, methodDescription) -> {
                    final @NotNull var field = Objects.requireNonNull(context.getInstrumentedType().getDeclaredFields().getFirst(), "cannot found field");
                    final @NotNull var type = field.getDeclaringType();
                    final @NotNull Label labelB = new Label();
                    final @NotNull Label labelC = new Label();

                    // A label -  Call super init and check instanceof
                    methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
                    methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object",
                            "<init>", "()V", false);
                    methodVisitor.visitVarInsn(Opcodes.ALOAD, 1); // Load constructor argument
                    methodVisitor.visitTypeInsn(Opcodes.INSTANCEOF, type.getInternalName()); // Do instanceof check
                    methodVisitor.visitJumpInsn(Opcodes.IFEQ, labelB); // If failed instanceof check, Goto B
                    // Put cast result to field
                    methodVisitor.visitVarInsn(Opcodes.ALOAD, 0); // Load this
                    methodVisitor.visitVarInsn(Opcodes.ALOAD, 1); // Load constructor argument
                    // Force cast & put field
                    methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, type.getInternalName());
                    methodVisitor.visitFieldInsn(Opcodes.PUTFIELD, context.getInstrumentedType().getInternalName(),
                            field.getInternalName(), type.getInternalName());
                    // goto C (return) to skipping B label
                    methodVisitor.visitJumpInsn(Opcodes.GOTO, labelC);

                    // B label - throw exception because failed instanceof check at A label
                    final @NotNull String sb = "java/lang/StringBuilder";
                    methodVisitor.visitLabel(labelB);
                    // throw new IllegalArgumentException("Except string but was found " + v1);
                    methodVisitor.visitTypeInsn(Opcodes.NEW, "java/lang/IllegalArgumentException");
                    methodVisitor.visitInsn(Opcodes.DUP);
                    methodVisitor.visitTypeInsn(Opcodes.NEW, sb);
                    methodVisitor.visitInsn(Opcodes.DUP);
                    methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, sb, "<init>", "()V", false);
                    methodVisitor.visitLdcInsn("Except string but was found ");
                    methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, sb, "append", "(Ljava/lang/String;)L" + sb + ";", false);
                    methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
                    methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, sb, "append", "(Ljava/lang/Object;)L" + sb + ";", false);
                    methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, sb, "toString", "()Ljava/lang/String;", false);
                    methodVisitor.visitMethodInsn(
                            Opcodes.INVOKESPECIAL, "java/lang/IllegalArgumentException",
                            "<init>", "(Ljava/lang/String;)V", false);
                    methodVisitor.visitInsn(Opcodes.ATHROW);

                    // C return
                    methodVisitor.visitLabel(labelC);
                    methodVisitor.visitInsn(Opcodes.RETURN);

                    return new ByteCodeAppender.Size(3, 2);
                })))
                .method(ElementMatchers.anyOf(ACCESSOR_OBJECT_GETTER))
                .intercept(FieldAccessor.ofField("targetInstance"))
                .method(ElementMatchers.anyOf(ACCESSOR_TARGET_CLASS_GETTER))
                .intercept(FixedValue.value(targetClass));

        // TODO

        // Load and make constructor function
        final @NotNull Class<? extends T> loaded = definition
                .make()
                .load(accessor.getClassLoader())
                .getLoaded();
        final @NotNull MethodHandles.Lookup lookup = MethodHandles.lookup();
        final @NotNull MethodHandle constructor = lookup.findConstructor(
                loaded, MethodType.methodType(void.class, Object.class));
        final @NotNull MethodType functionMethodType = MethodType.methodType(Object.class, Object.class);
        CallSite callSite = LambdaMetafactory.metafactory(
                lookup,
                "apply",
                MethodType.methodType(Function.class),
                functionMethodType,
                constructor,
                functionMethodType
        );
        @SuppressWarnings("unchecked")
        final @NotNull Function<Object, T> function = (Function<Object, T>) callSite.getTarget().invoke();
        return function;
    }

    private void visitAccessorMethod(final @NotNull Method method) {
        final @NotNull Class<?> returnType = method.getReturnType();
        final boolean isVoid = returnType == void.class;
        Object object = null;
        final @Nullable Accessor.TypeIndex typeIndex = method.getAnnotation(Accessor.TypeIndex.class);
        if (typeIndex != null) {
            if (typeIndex.targetType() != Accessor.TargetType.METHOD && isVoid) {
                throw new IllegalArgumentException("Cannot use TargetType that isn't TargetType.METHOD for void type!");
            }
            final Pair<IndexedData[], @Nullable IndexedData> result = find(getFixedTarget(method.getReturnType()), typeIndex);
            if (result.second() == null) {
                if (result.first().length == 0) {
                    throw new IllegalArgumentException("Cannot found any methods/fields for type " + getFixedTarget(method.getReturnType()));
                } else {
                    final @NotNull StringBuilder sb = new StringBuilder("Cannot found any methods/fields for index " + typeIndex.value() + ". Possible values:");
                    for (int i = 0; i < result.first().length; i++) {
                        final @NotNull IndexedData data = result.first()[i];
                        sb
                                .append("\n  - ").append(i).append(": ")
                                .append(data.name).append(" (").append(data.type.getName()).append(", ").append(data.object.getClass().getSimpleName()).append(")");
                    }
                    throw new IndexOutOfBoundsException(sb.toString());
                }
            }
            if (result.second().nullable() && method.isAnnotationPresent(NotNull.class)) {
                throw new IllegalArgumentException("Target is nullable, But accessor's method has marked @NotNull");
            }
            object = result.second().object;
        }
        // TODO
    }

    private Class<?> getFixedTarget(final @NotNull Class<?> targetClass) {
        if (IAccessor.class.isAssignableFrom(targetClass)) {
            final @NotNull Accessor annotation = targetClass.getAnnotation(Accessor.class);
            if (annotation == null) throw new IllegalArgumentException("Try to access class " + targetClass.getName() + " as accessor but that does not have @Accessor");
            return getFixedTarget(annotation.value());
        }
        return targetClass;
    }

    private @NotNull Pair<IndexedData[], @Nullable IndexedData> find(
            final @NotNull Class<?> type, final @NotNull Accessor.TypeIndex annotation) {
        final @NotNull Method[] methods = annotation.declared() ? this.targetDeclaredMethods : this.targetMethods;
        final @NotNull Field[] fields = annotation.declared() ? this.targetDeclaredFields : this.targetFields;
        final @NotNull Accessor.TargetType targetType = annotation.targetType();
        final @NotNull Object[] objects = switch (targetType) {
            case FIELD -> fields;
            case METHOD -> methods;
            default -> {
                final @NotNull Object[] copy = new Object[methods.length + fields.length];
                final Pair<Object[], Object[]> pair = switch (targetType) {
                    case METHOD_FIRST -> Pair.of(methods, fields);
                    case FIELD_FIRST -> Pair.of(fields, methods);
                    default -> throw new IllegalArgumentException("Impossible switch branch");
                };
                System.arraycopy(pair.first(), 0, copy, 0, pair.first().length);
                System.arraycopy(pair.second(), 0, copy, pair.first().length, pair.second().length);
                yield copy;
            }
        };
        final IndexedData[] dataArray = new IndexedData[objects.length];
        for (int i = 0; i < objects.length; i++) {
            if (objects[i] instanceof Method method)
                dataArray[i] = IndexedData.of(method, i);
            else if (objects[i] instanceof Field field)
                dataArray[i] = IndexedData.of(field, i);
            else {
                throw new IllegalArgumentException("Contains unknown data that cannot be IndexedData: " + objects[i]);
            }
        }
        final IndexedData[] result = Arrays
                .stream(dataArray)
                .filter(it -> it.type == type || type.isAssignableFrom(it.type))
                .toArray(IndexedData[]::new);
        return Pair.of(result, result.length == 0 || annotation.value() >= result.length ? null : result[annotation.value()]);
    }

    private record IndexedData(@NotNull Object object, int index, Class<?> type,
                               @NotNull String name, @NotNull Class<?>[] parameter,
                               boolean primitive, boolean notNull, boolean nullable) {
        private static final @NotNull Class<?>[] EMPTY_PARAMETER = new Class<?>[0];

        public static @NotNull IndexedData of(final @NotNull Method method, final int index) {
            final @NotNull Class<?> type = method.getReturnType();
            final boolean primitive = AccessorFactory.isPrimitive(type);
            final boolean notNull = primitive || method.isAnnotationPresent(NotNull.class);
            final boolean nullable = !notNull && method.isAnnotationPresent(Nullable.class);
            return new IndexedData(method, index, type, method.getName(), method.getParameterTypes(), primitive, notNull, nullable);
        }

        public static @NotNull IndexedData of(final @NotNull Field field, final int index) {
            final @NotNull Class<?> type = field.getType();
            final boolean primitive = AccessorFactory.isPrimitive(type);
            final boolean notNull = primitive || field.isAnnotationPresent(NotNull.class);
            final boolean nullable = !notNull && field.isAnnotationPresent(Nullable.class);
            return new IndexedData(field, index, type, field.getName(), EMPTY_PARAMETER, primitive, notNull, nullable);
        }
    }
}
