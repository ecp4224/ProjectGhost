package me.eddiep.ghost.client.core.events;

import static me.eddiep.ghost.client.core.events.StandardEvent.*;

import me.eddiep.ghost.client.core.Entity;
import org.jetbrains.annotations.NotNull;

public interface Event {
    Event[] EVENTS = new Event[] {
            FireGun,
            FireBoomerang,
            BoomerangCatch,
            FireCircle,
            LaserCharge,
            FireLaser
    };

    short getID();

    void trigger(@NotNull Entity cause, double direction);
}
