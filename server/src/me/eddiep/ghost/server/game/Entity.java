package me.eddiep.ghost.server.game;

import me.eddiep.ghost.server.Main;
import me.eddiep.ghost.server.game.util.Vector2f;
import me.eddiep.ghost.server.utils.PFunction;
import me.eddiep.ghost.server.utils.TimeUtils;

import java.io.IOException;

import static me.eddiep.ghost.server.utils.Constants.UPDATE_STATE_INTERVAL;

public abstract class Entity {
    protected Vector2f position;
    protected Vector2f velocity;
    protected double rotation;
    protected Entity parent;
    protected ActiveMatch containingMatch;
    protected String name;
    protected int alpha;
    public boolean oldVisibleState;
    private short ID = -1;
    private long lastUpdate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public ActiveMatch getMatch() {
        return containingMatch;
    }

    public void setMatch(ActiveMatch containingMatch) {
        this.containingMatch = containingMatch;
    }

    public boolean isInMatch() {
        return containingMatch != null;
    }

    public Entity getParent() {
        return parent;
    }

    public void setParent(Entity parent) {
        this.parent = parent;
    }

    public Vector2f getPosition() {
        return position;
    }

    public void setPosition(Vector2f position) {
        this.position = position;
    }

    public Vector2f getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2f velocity) {
        this.velocity = velocity;
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public float getXVelocity() {
        return velocity.x;
    }

    public float getYVelocity() {
        return velocity.y;
    }

    public void setVelocity(float xvel, float yvel) {
        setVelocity(new Vector2f(xvel, yvel));
    }

    public void resetUpdateTimer() {
        lastUpdate = 0;
    }

    public void tick() {
        if (getMatch().getTimeElapsed() - lastUpdate >= UPDATE_STATE_INTERVAL) {
            lastUpdate = getMatch().getTimeElapsed();
            try {
                updateState();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void setID(short ID) {
        this.ID = ID;
    }

    public short getID() {
        return ID;
    }

    public boolean isInside(float xmin, float ymin, float xmax, float ymax) {
        return position.x >= xmin && position.y >= ymin && position.x <= xmax && position.y <= ymax;
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = (byte) (alpha * 255);
    }

    public boolean isVisible() {
        return alpha > 0;
    }

    public void setVisible(boolean visible) {
        if (visible)
            alpha = 255;
        else
            alpha = 0;
    }

    public abstract void updateState() throws IOException;

    public void fadeOut(long duration) {
        fadeOut(false, duration);
    }

    public void fadeOutAndDespawn(long duration) {
        fadeOut(true, duration);
    }

    public void fadeOut(final boolean despawn, final long duration) {
        final long start = System.currentTimeMillis();
        TimeUtils.executeWhile(new Runnable() {
            @Override
            public void run() {
                alpha = (int) TimeUtils.ease(255, 0, duration, System.currentTimeMillis() - start);

                if (alpha == 0 && despawn) {
                    try {
                        getMatch().despawnEntity(Entity.this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new PFunction<Void, Boolean>() {
            @Override
            public Boolean run(Void val) {
                return alpha > 0f;
            }
        }, 16);
    }

    public void shake(long duration) {
        shake(duration, 50, 0.2f);
    }

    public void shake(final long duration, final double shakeWidth, final double shakeIntensity) {
        final float ox = getX();
        final float oy = getY();
        final long start = System.currentTimeMillis();
        final int rand1 = Main.RANDOM.nextInt(), rand2 = Main.RANDOM.nextInt();

        TimeUtils.executeUntil(new Runnable() {
            @Override
            public void run() {
                float xadd = (float) (Math.cos(System.currentTimeMillis() + rand1 * shakeWidth) / shakeIntensity);
                float yadd = (float) (Math.sin(System.currentTimeMillis() + rand2 * shakeWidth) / shakeIntensity);

                setPosition(new Vector2f(ox + xadd, oy + yadd));
            }
        }, new PFunction<Void, Boolean>() {
            @Override
            public Boolean run(Void val) {
                return System.currentTimeMillis() - start >= duration;
            }
        }, 16);
    }
}
