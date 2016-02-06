package me.eddiep.ghost.client.core.game.events;

import me.eddiep.ghost.client.core.game.Entity;
import me.eddiep.ghost.client.core.render.scene.impl.SpriteScene;
import org.jetbrains.annotations.NotNull;

import static me.eddiep.ghost.client.core.game.events.StandardEvent.*;

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

    void trigger(@NotNull Entity cause, double direction, @NotNull SpriteScene world);
}
