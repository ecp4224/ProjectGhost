package com.boxtrotstudio.ghost.client.core.game.animations;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxtrotstudio.ghost.client.core.game.SpriteEntity;
import com.boxtrotstudio.ghost.client.utils.ArrayHelper;
import com.boxtrotstudio.ghost.client.utils.Direction;
import com.boxtrotstudio.ghost.client.utils.PrimitiveDefaults;
import com.boxtrotstudio.ghost.client.utils.builder.Binder;
import com.boxtrotstudio.ghost.client.utils.builder.Builder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Animation {
    public static final int FIRST_FRAME = 0;
    public static final int LAST_FRAME = -1;

    private AnimationType type;
    private Direction direction;
    private int x;
    private int y;
    private int width;
    private int height;
    private int framecount;
    private int speed;
    private boolean reverse = false;
    private int[] sequence = new int[0];
    private List<AnimationVariant> variants = new ArrayList<>();
    private HashMap<String, Object> extraData = new HashMap<>();

    private volatile TextureRegion textureRegion;
    private volatile int currentFrame;
    private volatile long currentTick;
    private volatile int lastFrame;
    private volatile boolean paused = false;
    private volatile AnimationVariant currentVariant;
    private volatile SpriteEntity parent;
    private volatile boolean isPlayingReverse;
    private volatile Runnable completed;

    private volatile Runnable frameListener;
    private volatile int frameNumberListener;

    private volatile AnimationType nextAnimationType = AnimationType.NONE;

    private boolean hold;

    public static AnimationBuilder newBuilder() {
        return Binder.newBinderObject(AnimationBuilder.class);
    }

    public void init() {
        if (getVariant("DEFAULT") != null)
            throw new IllegalAccessError("This animation has already been initialized");

        AnimationVariant defaultVariant = AnimationVariant.fromAnimation(this);
        defaultVariant.setName("DEFAULT");
        variants.add(defaultVariant);
        this.currentVariant = defaultVariant;
    }

    public void attach(SpriteEntity parent) {
        if (textureRegion != null)
            throw new IllegalAccessError("This Animation is already attached to a texture!");

        textureRegion = new TextureRegion(parent.getTexture(), x, y, width, height);
        this.parent = parent;
    }

    private Animation() { }

    Animation(AnimationType type, Direction direction, int x, int y,
              int width, int height, int framecount, int speed, boolean reverse,
              int[] sequence, List<AnimationVariant> variants) {
        this.type = type;
        this.direction = direction;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.framecount = framecount;
        this.speed = speed;
        this.reverse = reverse;
        this.sequence = sequence;
        this.variants = variants;
    }

    public boolean hasSequence() {
        return sequence.length > 0;
    }

    public boolean tick() {
        currentTick += (isPlayingReverse ? -1 : 1);
        double tickPerFrame = 60.0 / speed;
        currentFrame = (int)(currentTick / tickPerFrame);

        int count = framecount;
        if (sequence.length > 0)
            count = sequence.length;

        if (currentFrame >= count && hold) {
            currentFrame = count - 1;
        }

        if (frameListener != null && frameNumberListener == currentFrame) {
            frameListener.run();
            frameListener = null;
        }

        if (currentFrame >= count && !hold) {
            if (reverse)
                reverse();
            else {
                currentFrame = 0;
                currentTick = 0;
                textureRegion.setRegion(x, y, width, height);
                if (completed != null) {
                    completed.run();
                    completed = null;
                    nextAnimationType = AnimationType.NONE;
                }
            }
            return true;
        } else if (currentFrame < 0) {
            isPlayingReverse = false;
            currentFrame = 0;
            currentTick = 0;

            textureRegion.setRegion(x, y, width, height);
            if (completed != null) {
                completed.run();
                completed = null;
                nextAnimationType = AnimationType.NONE;
            }
            return true;
        } else if (lastFrame != currentFrame || hold) {
            int frameToShow = currentFrame;
            if (sequence.length > 0) {
                frameToShow = sequence[currentFrame]-1;
            }
            textureRegion.setRegion(x + (width * frameToShow), y, width, height);
            lastFrame = currentFrame;
            return true;
        }

        return false;
    }

    public <T> T getData(String dataName) {
        return (T)extraData.get(dataName);
    }

    void setType(AnimationType type) {
        this.type = type;
    }

    void setDirection(Direction direction) {
        this.direction = direction;
    }

    void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    void setSequence(int[] sequence) {
        this.sequence = sequence;
    }

    void setVariants(List<AnimationVariant> variants) {
        this.variants = variants;
    }

    public AnimationType getType() {
        return type;
    }

    public Direction getDirection() {
        return direction;
    }

    void setX(int x) {
        this.x = x;
    }


    void setY(int y) {
        this.y = y;
    }

    void setWidth(int width) {
        this.width = width;
    }

    void setHeight(int height) {
        this.height = height;
    }

    void setFramecount(int framecount) {
        this.framecount = framecount;
    }

    void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getFramecount() {
        return framecount;
    }

    public int getSpeed() {
        return speed;
    }

    public int getCurrentFrame() {
        return currentFrame;
    }

    public void setCurrentFrame(int currentFrame) {
        this.currentFrame = currentFrame;
    }

    public Animation play() {
        isPlayingReverse = false;
        paused = false;
        parent.setCurrentAnimation(this);
        return this;
    }

    public Animation reverse() {
        if (!isPlaying())
            play();
        isPlayingReverse = true;

        return this;
    }

    public Animation setFrame(int frame) {
        if (frame < 0) {
            frame = Math.abs(frame) - 1;
            setFrame(framecount - frame);
            return this;
        }

        double tickPerFrame = 60.0 / speed;
        currentTick = (long) (frame * tickPerFrame);
        return this;
    }

    public Animation pause() {
        paused = true;
        return this;
    }

    public Animation stop() {
        currentFrame = 0;
        currentTick = 0;
        paused = false;
        lastFrame = 0;
        textureRegion.setRegion(x, y, width, height);
        return this;
    }

    public List<AnimationVariant> getVariants() {
        return Collections.unmodifiableList(variants);
    }

    public void applyVariant(AnimationVariant animationVariant) {
        if (animationVariant == null)
            animationVariant = getVariant("DEFAULT");

        this.currentVariant = animationVariant;
        animationVariant.applyTo(this);
    }

    public AnimationVariant getVariant(String name) {
        for (AnimationVariant variant : variants) {
            if (variant.getName().equals(name))
                return variant;
        }

        return null;
    }

    public TextureRegion getTextureRegion() {
        return textureRegion;
    }

    public Animation reset() {
        stop();
        return this;
    }

    public Animation onComplete(Runnable runnable) {
        this.completed = runnable;
        this.nextAnimationType = AnimationType.NONE;
        return this;
    }

    public Animation executeOnFrame(int frameNumber, Runnable runnable) {
        this.frameListener = runnable;
        this.frameNumberListener = frameNumber;
        return this;
    }

    public Animation onCompletePlay(AnimationType type) {
        return onCompletePlay(type, direction);
    }

    public Animation onCompletePlay(final AnimationType type, final Direction direction) {
        onComplete(() -> parent.getAnimation(type, direction).reset().play());

        this.nextAnimationType = type;

        return this;
    }

    public boolean isPlaying() {
        return parent.getCurrentAnimation().equals(this);
    }

    public AnimationType getNextAnimationType() {
        return nextAnimationType;
    }

    public void holdOnComplete() {
        this.hold = true;
    }

    public boolean isOrWillBe(@NotNull AnimationType... possible) {
        //Is it one of the types or will it be one of the types?
        return ArrayHelper.contains(possible, type) || ArrayHelper.contains(possible, nextAnimationType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Animation animation = (Animation) o;

        if (type != animation.type) return false;
        if (direction != animation.direction) return false;
        return parent != null ? parent.equals(animation.parent) : animation.parent == null;

    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (direction != null ? direction.hashCode() : 0);
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        return result;
    }
}
