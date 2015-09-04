package me.eddiep.ghost.game.match.world.physics;

import me.eddiep.ghost.game.match.entities.Entity;

public interface PhysicsEntity extends Entity {

    Hitbox getHitbox();
}
