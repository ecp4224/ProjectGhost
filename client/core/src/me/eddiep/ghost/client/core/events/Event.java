package me.eddiep.ghost.client.core.events;

import me.eddiep.ghost.client.core.Entity;
import org.jetbrains.annotations.NotNull;

import static me.eddiep.ghost.client.core.events.StandardEvent.*;

public interface Event {
    Event[] EVENTS = new Event[] {
            FireGun,
            FireBoomerang,
            BoomerangCatch,
            FireCircle,
            LaserCharge,
            FireLaser,
            ItemPickUp,
            FireDash,
            PlayerHit,
            PlayerDeath
    };

    short getID();

    void trigger(@NotNull Entity cause, double direction);
}
