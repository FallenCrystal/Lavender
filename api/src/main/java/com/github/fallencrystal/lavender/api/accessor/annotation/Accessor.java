package com.github.fallencrystal.lavender.api.accessor.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Accessor {
    @NotNull Class<?> value() default Object.class;
    @NotNull String className() default "";

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface TypeIndex {
        int value();
        boolean declared() default false;
        @NotNull TargetType targetType() default TargetType.METHOD;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface NamedTarget {
        @NotNull String value() default "";
        boolean declared() default false;
        @NotNull TargetType targetType() default TargetType.METHOD;
    }

    enum TargetType {
        METHOD,
        FIELD,
        METHOD_FIRST,
        FIELD_FIRST
    }
}
