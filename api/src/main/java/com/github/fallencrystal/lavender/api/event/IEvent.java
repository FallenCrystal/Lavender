package com.github.fallencrystal.lavender.api.event;

import lombok.Getter;
import lombok.Setter;

public interface IEvent {
    interface Cancellable extends IEvent {
        boolean isCancelled();
        void setCancelled(boolean cancel);
    }

    @Getter
    @Setter
    abstract class AbstractCancellable implements Cancellable {
        private boolean cancelled = false;
    }
}
