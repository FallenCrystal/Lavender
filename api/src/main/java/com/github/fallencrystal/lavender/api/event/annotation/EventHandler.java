package com.github.fallencrystal.lavender.api.event.annotation;

import com.github.fallencrystal.lavender.api.event.EventPriority;
import com.github.fallencrystal.lavender.api.event.IEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@SuppressWarnings("unused")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventHandler {
    @NotNull EventPriority priority() default EventPriority.NORMAL;
    @NotNull Class<? extends IEvent> value() default IEvent.class;
    @NotNull String className() default "";
    @NotNull NotFoundAction whenNotFound() default NotFoundAction.EXCEPTION;
    @NotNull InjectParameter[] parameters() default {};

    enum NotFoundAction {
        WARNING,
        IGNORING,
        EXCEPTION
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    @interface InjectParameter {
        int index() default -1;
        Class<?> exceptedType() default Object.class;
        @NotNull ParameterFailedAction onFailed() default ParameterFailedAction.ALWAYS_EXCEPTION;
        boolean checkNotNull() default true;
    }

    enum ParameterFailedAction {
        ALWAYS_EXCEPTION,
        NULL_OR_EXCEPTION,
        SKIPPING_REGISTER
    }
}
