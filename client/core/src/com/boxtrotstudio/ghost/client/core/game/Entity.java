package com.boxtrotstudio.ghost.client.core.game;

import com.boxtrotstudio.ghost.client.core.logic.Logical;
import com.boxtrotstudio.ghost.client.core.render.Drawable;
import com.boxtrotstudio.ghost.client.utils.Vector2f;
import org.jetbrains.annotations.NotNull;

public interface Entity extends Drawable, Logical, Attachable, Comparable<Entity> {

    short getID();

    int getZ();

    float getCenterX();

    float getCenterY();

    float getAlpha();

    Vector2f getVelocity();

    Vector2f getTarget();

    void setID(short id);

    void setZ(int z);

    void setVelocity(Vector2f velocity);

    void setTarget(Vector2f target);

    boolean isVisible();

    void setVisible(boolean visible);

    boolean hasLighting();

    void setHasLighting(boolean lighting);

    void unload();

    void interpolateTo(float x, float y, long duration);

    default boolean contains(float x, float y) {
        return (x >= getX() && x <= getX() + getWidth() && y >= getY() && y <= getY() + getHeight());
    }

    default int compareTo(@NotNull Entity o) {
        return getZ() - o.getZ();
    }

    float getWidth();

    float getHeight();

    void setWidth(float width);

    void setHeight(float height);

    float getRotation();

    void setRotation(float rotation);

    void setCenterX(float x);

    void setCenterY(float y);

    void setCenter(float x, float y);

    void setSize(float width, float height);
}