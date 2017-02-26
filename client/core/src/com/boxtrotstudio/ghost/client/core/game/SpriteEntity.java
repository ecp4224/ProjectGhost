package com.boxtrotstudio.ghost.client.core.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.boxtrotstudio.ghost.client.Ghost;
import com.boxtrotstudio.ghost.client.core.game.animations.Animation;
import com.boxtrotstudio.ghost.client.core.game.animations.AnimationType;
import com.boxtrotstudio.ghost.client.core.physics.Face;
import com.boxtrotstudio.ghost.client.core.render.Blend;
import com.boxtrotstudio.ghost.client.handlers.scenes.SpriteScene;
import com.boxtrotstudio.ghost.client.utils.Direction;
import com.boxtrotstudio.ghost.client.utils.Vector2f;
import com.boxtrotstudio.ghost.client.utils.annotations.InternalOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SpriteEntity extends Sprite implements Entity {
    private int z;
    private boolean hasLoaded = false;
    private short id;

    private Vector2f velocity = new Vector2f(0f, 0f);
    private Vector2f target = null;
    private boolean isMoving;
    private String path;

    private Animation animation;
    private List<Animation> animations = new ArrayList<>();

    private Vector2f inter_target, inter_start;
    private long inter_duration, inter_timeStart;
    private boolean interpolate = false;
    private Blend blend = Blend.DEFAULT;

    private ArrayList<Attachable> children = new ArrayList<Attachable>();
    private ArrayList<Attachable> parents = new ArrayList<Attachable>();

    private final Object child_lock = new Object();
    protected boolean lightable = true;
    private SpriteScene scene;

    private boolean isVisible = true;

    public static SpriteEntity fromImage(String path) {
        Texture texture = Ghost.ASSETS.get(path, Texture.class);
        Sprite sprite = new Sprite(texture);
        SpriteEntity e = new SpriteEntity(sprite, (short)0);
        e.path = path;
        return e;
    }

    public static SpriteEntity fromImage(String path, short id) {
        Texture texture = Ghost.ASSETS.get(path, Texture.class);
        Sprite sprite = new Sprite(texture);
        SpriteEntity e = new SpriteEntity(sprite, id);
        e.path = path;
        return e;
    }

    public static SpriteEntity fromImage(FileHandle file) {
        Texture texture = new Texture(file);
        Sprite sprite = new Sprite(texture);
        SpriteEntity e = new SpriteEntity(sprite, (short)0);
        e.path = file.name();
        return e;
    }

    public static SpriteEntity fromImage(FileHandle file, short id) {
        Texture texture = new Texture(file);
        Sprite sprite = new Sprite(texture);
        SpriteEntity e = new SpriteEntity(sprite, id);
        e.path = file.path();
        return e;
    }

    public SpriteEntity(Sprite sprite, short id) {
        super(sprite);
        this.path = sprite.toString();

        setOriginCenter();

        this.id = id;
    }

    public SpriteEntity(String path, short id) {
        super(Ghost.ASSETS.get(path, Texture.class));

        this.path = path;

        setOriginCenter();

        this.id = id;
    }

    @Override
    public int getZ() {
        return z;
    }

    @Override
    public void setZ(int z) {
        this.z = z;
    }

    public boolean isMoving() {
        return isMoving;
    }

    public void setMoving(boolean moving) {
        isMoving = moving;
    }

    @Override
    public Blend blendMode() {
        return blend;
    }

    @Override
    public boolean hasLighting() {
        return lightable;
    }

    @Override
    public int getZIndex() {
        return z;
    }

    @Override
    public boolean isVisible() {
        return isVisible && getAlpha() > 0f;
    }

    @Override
    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    @Override
    public void setHasLighting(boolean val) {
        boolean update = this.lightable != val;

        this.lightable = val;

        if (hasLoaded && update) {
            //We need to reload this sprite now
            scene.removeEntity(this);
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    scene.addEntity(SpriteEntity.this);
                }
            });
        }
    }

    public void setBlend(Blend blend) {
        this.blend = blend;
    }

    @Override
    public short getID() {
        return id;
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
    public void setTarget(Vector2f target) {
        this.target = target;
    }

    @Override
    public Vector2f getTarget() {
        return target;
    }

    @Override
    public void setID(short id) {
        this.id = id;
    }

    @Override
    public float getCenterX() {
        return getX() + (getWidth() / 2f);
    }

    @Override
    public float getCenterY() {
        return getY() + (getHeight() / 2f);
    }

    @Override
    public float getAlpha() { return getColor().a; }

    public String getPath() {
        return path;
    }

    @Override
    @InternalOnly
    public final void load() {
        onLoad();
        if (!hasLoaded)
            throw new IllegalStateException("super.onLoad() was not invoked!");
    }

    protected void onLoad() {
        hasLoaded = true;
    }

    @Override
    @InternalOnly
    public final void unload() {
        onUnload();
        if (hasLoaded)
            throw new IllegalStateException("super.onUnload() was not invoked!");
    }

    protected void onUnload() {
        hasLoaded = false;
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);
    }

    @Override
    public void setX(float x) {
        float dif = getX() - x;
        super.setX(x);

        synchronized (child_lock) {
            for (Attachable c : children) {
                c.setX(c.getX() - dif);
            }
        }
    }

    @Override
    public void setAlpha(float alpha) {
        super.setAlpha(alpha);

        if (path.contains("flag")) {
            new Exception().printStackTrace();
        }

        synchronized (child_lock) {
            for (Attachable c : children) {
                c.setAlpha(alpha);
            }
        }
    }

    @Override
    public void setY(float y) {
        float dif = getY() - y;
        super.setY(y);

        synchronized (child_lock) {
            for (Attachable c : children) {
                c.setY(c.getY() - dif);
            }
        }
    }

    @Override
    public void attach(Attachable e) {
        synchronized (child_lock) {
            children.add(e);
        }
        e.addParent(this);
    }

    @Override
    public void deattach(Attachable e) {
        synchronized (child_lock) {
            children.remove(e);
        }
        e.removeParent(this);
    }

    @Override
    public void addParent(Attachable e) {
        parents.add(e);
    }

    @Override
    public void removeParent(Attachable e) {
        parents.remove(e);
    }

    @Override
    public void tick() {
        if (!interpolate) {
            if (target != null) {
                if (Math.abs(getCenterX() - target.x) < 8 && Math.abs(getCenterY() - target.y) < 8) {
                    velocity.x = velocity.y = 0;
                    target = null;
                }
            }

            setX(getX() + velocity.x);
            setY(getY() + velocity.y);
        } else {
            float x = ease(inter_start.x, inter_target.x, System.currentTimeMillis() - inter_timeStart, inter_duration);
            float y = ease(inter_start.y, inter_target.y, System.currentTimeMillis() - inter_timeStart, inter_duration);

            setX(x);
            setY(y);

            if (x == inter_target.x && y == inter_target.y) {
                interpolate = false;
            }
        }

        if (isFadingOut) {
            float alpha = ease(1f, 0f, fadeDuration, System.currentTimeMillis() - fadeStart);

            setAlpha(alpha);

            if (alpha == 0f) {
                isFadingOut = false;
                if (isFadeOutDespawn) {
                    scene.removeEntity(this);
                }
            }
        }

        if (animation != null) {
            animation.tick();
            setRegion(animation.getTextureRegion());
        }
    }

    @Override
    public void dispose() { }


    @Override
    public SpriteScene getParentScene() {
        return scene;
    }

    @Override
    public void setParentScene(SpriteScene scene) {
        this.scene = scene;
    }

    public void setZIndex(int z) {
        this.z = z;

        if (hasLoaded) {
            //We need to reload this sprite now
            scene.removeEntity(this);
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    scene.addEntity(SpriteEntity.this);
                }
            });
        }
    }

    @Override
    public void interpolateTo(float x, float y, long duration) {
        inter_start = new Vector2f(getX(), getY());
        inter_target = new Vector2f(x, y);
        inter_timeStart = System.currentTimeMillis();
        inter_duration = duration;
        interpolate = true;
    }

    @Override
    public void setWidth(float width) {
        super.setSize(width, getHeight());
    }

    @Override
    public void setHeight(float height) {
        super.setSize(getWidth(), height);
    }

    public boolean hasAnimations() {
        return animations.size() > 0;
    }

    public Animation getAnimation(AnimationType type, Direction direction) {
        for (Animation animation : animations) {
            if (animation.getType() == type && animation.getDirection() == direction) {
                return animation;
            }
        }
        return null;
    }

    //Code taken from: https://code.google.com/p/replicaisland/source/browse/trunk/src/com/replica/replicaisland/Lerp.java?r=5
    //Because I'm a no good dirty scrub
    public static float ease(float start, float target, float duration, float timeSinceStart) {
        float value = start;
        if (timeSinceStart > 0.0f && timeSinceStart < duration) {
            final float range = target - start;
            final float percent = timeSinceStart / (duration / 2.0f);
            if (percent < 1.0f) {
                value = start + ((range / 2.0f) * percent * percent * percent);
            } else {
                final float shiftedPercent = percent - 2.0f;
                value = start + ((range / 2.0f) *
                        ((shiftedPercent * shiftedPercent * shiftedPercent) + 2.0f));
            }
        } else if (timeSinceStart >= duration) {
            value = target;
        }
        return value;
    }

    public Vector2f getPosition() {
        return new Vector2f(getCenterX(), getCenterY());
    }

    public Animation getCurrentAnimation() {
        return animation;
    }

    public void onMirrorHit(@NotNull Face closestFace, @NotNull Vector2f closestPoint) {
        double angle = Math.atan2(velocity.y, velocity.x);

        Vector2f temp = new Vector2f(1f, angle);
        Vector2f normal = closestFace.getNormal().cloneVector();
        Vector2f newVel = normal.scale(-2 * Vector2f.dot(temp, normal)).add(temp);
        //newVel.scale(velocity.length());
        /*double newAngle = Math.atan2(newVel.y, newVel.x);

        Vector2f normal = closestFace.getNormal();
        float p = Vector2f.dot(new Vector2f(velocity), normal)*-2f;
        Vector2f newVel = new Vector2f(normal.x, normal.y);
        newVel.scale(p);
        newVel.x += velocity.x;
        newVel.y += velocity.y;*/

        velocity = newVel;

        setCenter(closestPoint.x, closestPoint.y);
    }

    @Override
    public String toString() {
        return path;
    }

    public void setCurrentAnimation(Animation currentAnimation) {
        this.animation = currentAnimation;
        setSize(this.animation.getWidth(), this.animation.getHeight());
        setOriginCenter();
    }

    private boolean isFadingOut;
    private boolean isFadeOutDespawn;
    private long fadeDuration;
    private long fadeStart;
    public void fadeOutAndDespawn(long arg) {
        this.fadeDuration = arg;
        this.isFadingOut = true;
        this.isFadeOutDespawn = true;
        fadeStart = System.currentTimeMillis();
    }

    public void fadeOut(long arg) {
        this.fadeDuration = arg;
        this.isFadingOut = true;
        fadeStart = System.currentTimeMillis();
    }

    public void attachAnimations(Animation... animations) {
        for (Animation animation : animations) {
            animation.attach(this);
            this.animations.add(animation);
        }

        setCurrentAnimation(this.animations.get(0));
    }
}
