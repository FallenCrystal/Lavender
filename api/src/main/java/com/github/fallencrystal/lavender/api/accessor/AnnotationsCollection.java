package com.github.fallencrystal.lavender.api.accessor;

import com.github.fallencrystal.lavender.api.accessor.annotation.RuntimeGenerated;
import net.bytebuddy.description.annotation.AnnotationDescription;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;

@ApiStatus.Internal
@SuppressWarnings("unused")
public interface AnnotationsCollection {
    @NotNull AnnotationDescription NOT_NULL = empty(NotNull.class);
    @NotNull AnnotationDescription NULL_ABLE = empty(Nullable.class);
    @NotNull AnnotationDescription RUNTIME_GENERATED = empty(RuntimeGenerated.class);
    @NotNull AnnotationDescription API_INTERNAL = empty(ApiStatus.Internal.class);
    @NotNull AnnotationDescription API_EXPERIMENTAL = empty(ApiStatus.Experimental.class);

    static @NotNull AnnotationDescription empty(final @NotNull Class<? extends Annotation> annotationClass) {
        return AnnotationDescription.Builder.ofType(annotationClass).build();
    }
}
