package com.github.fallencrystal.lavender.api.accessor.interfaces;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@SuppressWarnings("unused")
public interface IAccessor {
    @SafetyIgnore @NotNull Object getObject();
    @SafetyIgnore @NotNull Class<?> getTargetClass();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface SafetyIgnore {}
}
