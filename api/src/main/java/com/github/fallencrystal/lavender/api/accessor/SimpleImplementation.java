package com.github.fallencrystal.lavender.api.accessor;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.jar.asm.MethodVisitor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimpleImplementation implements Implementation {
    private final @NotNull Apply applyFunc;

    @Override
    public @NotNull ByteCodeAppender appender(@NotNull Target target) {
        return applyFunc::apply;
    }

    @Override
    public @NotNull InstrumentedType prepare(@NotNull InstrumentedType instrumentedType) {
        return instrumentedType;
    }

    @FunctionalInterface
    public interface Apply {
        @NotNull
        ByteCodeAppender.Size apply(
                final @NotNull MethodVisitor methodVisitor,
                final @NotNull Context context,
                final @NotNull MethodDescription methodDescription);
    }

    public static @NotNull SimpleImplementation of(@NotNull Apply applyFunc) {
        return new SimpleImplementation(applyFunc);
    }
}
