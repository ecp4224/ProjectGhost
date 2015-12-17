package me.eddiep.ghost.client.core.events;

import me.eddiep.ghost.client.core.Entity;
import org.jetbrains.annotations.NotNull;

public interface Event {
    Event[] EVENTS = new Event[] {

    };

    short getID();

    void trigger(@NotNull Entity cause);
}
