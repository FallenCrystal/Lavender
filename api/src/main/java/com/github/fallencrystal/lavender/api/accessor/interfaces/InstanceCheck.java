package com.github.fallencrystal.lavender.api.accessor.interfaces;

import org.jetbrains.annotations.NotNull;

public interface InstanceCheck {
    boolean isInstance(Object instance);
    @NotNull Class<?> getTargetClass();
}
