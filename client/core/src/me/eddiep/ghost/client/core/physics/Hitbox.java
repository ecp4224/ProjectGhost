package me.eddiep.ghost.client.core.physics;


import me.eddiep.ghost.client.utils.Vector2f;

public interface Hitbox {
    boolean isPointInside(Vector2f point);

    CollisionResult isHitboxInside(Hitbox hitbox);

    boolean hasPolygon();

    Polygon getPolygon();

    String getName();
}
