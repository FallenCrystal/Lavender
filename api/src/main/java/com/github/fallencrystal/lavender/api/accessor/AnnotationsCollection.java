package com.github.fallencrystal.lavender.api.accessor;

import com.github.fallencrystal.lavender.api.accessor.annotation.RuntimeGenerated;
import net.bytebuddy.description.annotation.AnnotationDescription;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
@SuppressWarnings("unused")
public interface AnnotationsCollection {
    @NotNull AnnotationDescription NOT_NULL = AnnotationDescription.Builder.ofType(NotNull.class).build();
    @NotNull AnnotationDescription NULL_ABLE = AnnotationDescription.Builder.ofType(Nullable.class).build();
    @NotNull AnnotationDescription RUNTIME_GENERATED = AnnotationDescription.Builder.ofType(RuntimeGenerated.class).build();
    @NotNull AnnotationDescription API_INTERNAL = AnnotationDescription.Builder.ofType(ApiStatus.Internal.class).build();
    @NotNull AnnotationDescription API_EXPERIMENTAL = AnnotationDescription.Builder.ofType(ApiStatus.Experimental.class).build();
}
