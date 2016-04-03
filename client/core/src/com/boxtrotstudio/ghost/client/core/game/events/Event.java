package com.boxtrotstudio.ghost.client.core.game.events;

import com.boxtrotstudio.ghost.client.core.game.Entity;
import com.boxtrotstudio.ghost.client.handlers.scenes.SpriteScene;
import org.jetbrains.annotations.NotNull;

public interface Event {
    Event[] EVENTS = new Event[] {
            StandardEvent.FireGun,
            StandardEvent.FireBoomerang,
            StandardEvent.BoomerangCatch,
            StandardEvent.FireCircle,
            StandardEvent.LaserCharge,
            StandardEvent.FireLaser,
            StandardEvent.ItemPickUp,
            StandardEvent.FireDash,
            StandardEvent.PlayerHit,
            StandardEvent.PlayerDeath
    };

    short getID();

    void trigger(@NotNull Entity cause, double direction, @NotNull SpriteScene world);
}
