package com.github.fallencrystal.lavender.api.event.handler;

import com.github.fallencrystal.lavender.api.event.IEvent;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface IEventHandler<T extends IEvent> {
    void onEvent(@NotNull T event);
}
