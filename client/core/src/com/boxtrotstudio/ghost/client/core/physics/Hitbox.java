package com.boxtrotstudio.ghost.client.core.physics;


import com.boxtrotstudio.ghost.client.utils.Vector2f;

public interface Hitbox {
    boolean isPointInside(Vector2f point);

    CollisionResult isHitboxInside(Hitbox hitbox);

    boolean hasPolygon();

    Polygon getPolygon();

    String getName();
}
