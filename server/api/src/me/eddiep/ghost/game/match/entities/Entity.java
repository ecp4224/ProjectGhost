package me.eddiep.ghost.game.match.entities;

import me.eddiep.ghost.game.match.LiveMatch;
import me.eddiep.ghost.game.match.world.World;
import me.eddiep.ghost.utils.Vector2f;

public interface Entity {
    String getName();

    void setName(String name);

    LiveMatch getMatch();

    void setMatch(LiveMatch containingMatch);

    World getWorld();

    void setWorld(World world);

    boolean isInMatch();

    Entity getParent();

    void setParent(Entity parent);

    Vector2f getPosition();

    void setPosition(Vector2f position);

    Vector2f getVelocity();

    void setVelocity(Vector2f velocity);

    float getX();

    float getY();

    double getRotation();

    void setRotation(double rotation);

    float getXVelocity();

    float getYVelocity();

    void setVelocity(float xvel, float yvel);

    void tick();

    void setID(short ID);

    short getID();

    boolean isInside(float xmin, float ymin, float xmax, float ymax);

    int getAlpha();

    void setAlpha(int alpha);

    void setAlpha(float alpha);

    boolean isVisible();

    void setVisible(boolean visible);

    void fadeOut(long duration);

    void fadeOutAndDespawn(long duration);

    void fadeOut(final boolean despawn, final long duration);

    void shake(long duration);

    void shake(final long duration, final double shakeWidth, final double shakeIntensity);
}
