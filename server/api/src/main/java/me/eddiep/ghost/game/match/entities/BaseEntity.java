package me.eddiep.ghost.game.match.entities;

import me.eddiep.ghost.game.match.LiveMatch;
import me.eddiep.ghost.game.match.world.World;
import me.eddiep.ghost.utils.Global;
import me.eddiep.ghost.utils.PFunction;
import me.eddiep.ghost.utils.TimeUtils;
import me.eddiep.ghost.utils.Vector2f;

public abstract class BaseEntity implements Entity {
    protected Vector2f position;
    protected Vector2f velocity;
    protected double rotation;
    protected Entity parent;
    protected LiveMatch containingMatch;
    protected String name;
    protected int alpha;
    protected World world;
    protected boolean update = true;
    protected boolean requestTick = true;
    public boolean oldVisibleState;
    private short ID = -1;

    public boolean isSendingUpdates() {
        return update;
    }

    public void sendUpdates(boolean update) {
        this.update = update;
    }

    public boolean isRequestingTicks() {
        return requestTick;
    }

    public void requestTicks(boolean tick) {
        this.requestTick = tick;
    }

    @Override
    public void tick() {
        if (isFading) {
            alpha = (int) TimeUtils.ease(255, 0, fadeDuration, System.currentTimeMillis() - fadeStart);

            if (alpha == 0 && shouldFadeDespawn) {
                world.despawnEntity(this);
            }
        }
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public void setWorld(World world) {
        this.world = world;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public LiveMatch getMatch() {
        return containingMatch;
    }

    @Override
    public void setMatch(LiveMatch containingMatch) {
        this.containingMatch = containingMatch;
    }

    @Override
    public boolean isInMatch() {
        return containingMatch != null;
    }

    @Override
    public Entity getParent() {
        return parent;
    }

    @Override
    public void setParent(Entity parent) {
        this.parent = parent;
    }

    @Override
    public Vector2f getPosition() {
        return position;
    }

    @Override
    public void setPosition(Vector2f position) {
        this.position = position;
    }

    @Override
    public Vector2f getVelocity() {
        return velocity;
    }

    @Override
    public void setVelocity(Vector2f velocity) {
        this.velocity = velocity;
    }

    @Override
    public float getX() {
        return position.x;
    }

    @Override
    public float getY() {
        return position.y;
    }

    @Override
    public double getRotation() {
        return rotation;
    }

    @Override
    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    @Override
    public float getXVelocity() {
        return velocity.x;
    }

    @Override
    public float getYVelocity() {
        return velocity.y;
    }

    @Override
    public void setVelocity(float xvel, float yvel) {
        setVelocity(new Vector2f(xvel, yvel));
    }

    @Override
    public void setID(short ID) {
        this.ID = ID;
    }

    @Override
    public short getID() {
        return ID;
    }

    @Override
    public boolean isInside(float xmin, float ymin, float xmax, float ymax) {
        return position.x >= xmin && position.y >= ymin && position.x <= xmax && position.y <= ymax;
    }

    @Override
    public int getAlpha() {
        return alpha;
    }

    @Override
    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    @Override
    public void setAlpha(float alpha) {
        this.alpha = (byte) (alpha * 255);
    }

    @Override
    public boolean isVisible() {
        return alpha > 0;
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible)
            alpha = 255;
        else
            alpha = 0;
    }

    @Override
    public void fadeOut(long duration) {
        fadeOut(false, duration);
    }

    @Override
    public void fadeOutAndDespawn(long duration) {
        fadeOut(true, duration);
    }

    private boolean isFading = false;
    private boolean shouldFadeDespawn;
    private long fadeDuration;
    private long fadeStart;
    @Override
    public void fadeOut(final boolean despawn, final long duration) {
        fadeStart = System.currentTimeMillis();
        isFading = true;
        shouldFadeDespawn = despawn;
        fadeDuration = duration;
        /*TimeUtils.executeWhile(new Runnable() {
            @Override
            public void run() {
                alpha = (int) TimeUtils.ease(255, 0, duration, System.currentTimeMillis() - start);

                if (alpha == 0 && despawn) {
                    world.despawnEntity(BaseEntity.this);
                }
            }
        }, new PFunction<Void, Boolean>() {
            @Override
            public Boolean run(Void val) {
                return alpha > 0f;
            }
        }, 16);*/
    }

    @Override
    public void shake(long duration) {
        shake(duration, 50, 0.2f);
    }

    @Override
    public void shake(final long duration, final double shakeWidth, final double shakeIntensity) {
        final float ox = getX();
        final float oy = getY();
        final long start = System.currentTimeMillis();
        final int rand1 = Global.RANDOM.nextInt(), rand2 = Global.RANDOM.nextInt();

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
