package me.eddiep.ghost.game.match.entities;

import me.eddiep.ghost.game.match.Event;
import me.eddiep.ghost.game.match.LiveMatch;
import me.eddiep.ghost.game.match.world.World;
import me.eddiep.ghost.game.match.world.physics.PhysicsEntity;
import me.eddiep.ghost.utils.FastMath;
import me.eddiep.ghost.utils.Global;
import me.eddiep.ghost.utils.TimeUtils;
import me.eddiep.ghost.utils.Vector2f;

public abstract class BaseEntity implements Entity {
    protected Vector2f position = new Vector2f(0f, 0f);
    protected Vector2f velocity = new Vector2f(0f, 0f);
    protected double rotation;
    protected Entity parent;
    protected LiveMatch containingMatch;
    protected String name = "";
    protected int alpha;
    protected World world;
    protected boolean update = true;
    protected boolean requestTick = true;
    protected short width = -1, height = -1;
    protected boolean shouldCheckPhysics = true;
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

    public boolean isCheckingPhysics() {
        return shouldCheckPhysics;
    }

    public void checkPhysics(boolean shouldCheck) {
        this.shouldCheckPhysics = shouldCheck;
    }

    @Override
    public boolean doesBounce() {
        return false;
    }

    @Override
    public void tick() {
        if (isShaking) {
            float xadd = (float) (Math.cos(System.currentTimeMillis() + rand1 * shakeWidth) / shakeIntensity);
            float yadd = (float) (Math.sin(System.currentTimeMillis() + rand2 * shakeWidth) / shakeIntensity);

            setPosition(new Vector2f(shakeOriginX + xadd, shakeOriginY + yadd));

            if (System.currentTimeMillis() - shakeStart >= shakeDuration) {
                isShaking = false;
            }
        }
        if (isFading) {
            alpha = (int) TimeUtils.ease(255, 0, fadeDuration, System.currentTimeMillis() - fadeStart);

            if (alpha == 0 && shouldFadeDespawn) {
                world.despawnEntity(this);
                isFading = false;
            } else if (alpha == 0) {
                isFading = false;
            }
        }

        if (shouldCheckPhysics && world != null && world.getPhysics() != null) {
            if (world.getPhysics().checkEntity(this)) {
                hasEaseTarget = false;
            }
        }

        if (hasEaseTarget) {
            float x = FastMath.ease(startingEase.x, easeTarget.x, duration, (System.currentTimeMillis() - easeStart));
            float y = FastMath.ease(startingEase.y, easeTarget.y, duration, (System.currentTimeMillis() - easeStart));
            setPosition(new Vector2f(x, y));

            if (x == easeTarget.x && y == easeTarget.y) {
                hasEaseTarget = false;
            }
        }
    }

    @Override
    public short getWidth() {
        return width;
    }

    @Override
    public short getHeight() {
        return height;
    }

    @Override
    public void setWidth(short width) {
        this.width = width;
    }

    @Override
    public void setHeight(short height) {
        this.height = height;
    }

    @Override
    public boolean isDefaultSize() {
        return width == -1f && height == -1f;
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
    public boolean isEasing() {
        return hasEaseTarget;
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

    private boolean isShaking;
    private float shakeOriginX, shakeOriginY;
    private long shakeStart;
    private int rand1, rand2;
    private long shakeDuration;
    private double shakeWidth, shakeIntensity;
    @Override
    public void shake(final long duration, final double shakeWidth, final double shakeIntensity) {
        isShaking = true;
        shakeOriginX = getX();
        shakeOriginY = getY();
        shakeStart = System.currentTimeMillis();
        rand1 = Global.RANDOM.nextInt();
        rand2 = Global.RANDOM.nextInt();
        shakeDuration = duration;
        this.shakeWidth = shakeWidth;
        this.shakeIntensity = shakeIntensity;
    }

    @Override
    public void onCollision(PhysicsEntity entity) {
        world.despawnEntity(this);
    }

    @Override
    public boolean intersects(PlayableEntity player) {
        return player.getHitbox().isPointInside(getPosition());
    }

    @Override
    public void despawn() {
        if (world != null) {
            world.despawnEntity(this);
        }
    }

    @Override
    public void triggerEvent(Event event, double direction) {
        if (world == null)
            return;

        world.triggerEvent(event, this, direction);
    }

    private boolean hasEaseTarget;
    private Vector2f easeTarget;
    private Vector2f startingEase;
    private long duration;
    private long easeStart;
    @Override
    public void easeTo(Vector2f position, long duration) {
        this.hasEaseTarget = true;
        this.duration = duration;
        this.startingEase = getPosition().cloneVector();
        this.easeTarget = position;
        this.easeStart = System.currentTimeMillis();
    }
}
