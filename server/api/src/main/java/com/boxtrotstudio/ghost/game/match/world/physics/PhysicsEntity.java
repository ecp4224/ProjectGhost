package com.boxtrotstudio.ghost.game.match.world.physics;

import com.boxtrotstudio.ghost.game.match.entities.Entity;

public interface PhysicsEntity extends Entity {

    Hitbox getHitbox();
}
