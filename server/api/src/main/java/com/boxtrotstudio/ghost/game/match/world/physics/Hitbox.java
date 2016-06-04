package com.boxtrotstudio.ghost.game.match.world.physics;

import com.boxtrotstudio.ghost.utils.Vector2f;

public interface Hitbox {
    boolean isPointInside(Vector2f point);

    CollisionResult isHitboxInside(Hitbox hitbox);

    boolean hasPolygon();

    boolean isCollideable();

    Polygon getPolygon();

    String getName();
}
