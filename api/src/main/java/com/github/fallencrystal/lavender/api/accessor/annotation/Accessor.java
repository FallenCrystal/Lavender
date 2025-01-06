package com.github.fallencrystal.lavender.api.accessor.annotation;

import org.jetbrains.annotations.NotNull;

public @interface Accessor {
    @NotNull Class<?> value() default Object.class;
    @NotNull String className() default "";
}
