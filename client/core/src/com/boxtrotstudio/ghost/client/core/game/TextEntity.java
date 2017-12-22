package com.boxtrotstudio.ghost.client.core.game;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.boxtrotstudio.ghost.client.core.render.Text;
import com.boxtrotstudio.ghost.client.utils.Vector2f;

public class TextEntity extends Text implements Entity {
    private short id;
    private int z;
    private Vector2f velocity = new Vector2f(0f, 0f);
    private Vector2f target;

    private Vector2f inter_target, inter_start;
    private long inter_duration, inter_timeStart;
    private boolean interpolate;

    public TextEntity(int size, Color color, FileHandle file) {
        super(size, color, file);
    }

    public TextEntity(int size, Color color, FileHandle file, String characters) {
        super(size, color, file, characters);
    }

    public TextEntity(int size, Color color, FileHandle file, String characters, int align) {
        super(size, color, file, characters, align);
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
            float x = SpriteEntity.ease(inter_start.x, inter_target.x, System.currentTimeMillis() - inter_timeStart, inter_duration);
            float y = SpriteEntity.ease(inter_start.y, inter_target.y, System.currentTimeMillis() - inter_timeStart, inter_duration);

            setX(x);
            setY(y);

            if (x == inter_target.x && y == inter_target.y) {
                interpolate = false;
            }
        }

    }

    @Override
    public void dispose() { }

    @Override
    public short getID() {
        return id;
    }

    @Override
    public int getZ() {
        return z;
    }

    @Override
    public float getCenterX() {
        return x - (layout.width / 2f);
    }

    @Override
    public float getCenterY() {
        return y + (layout.height / 2f);
    }

    @Override
    public float getAlpha() {
        return color.a;
    }

    @Override
    public Vector2f getVelocity() {
        return velocity;
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
    public void setZ(int z) {
        this.z = z;
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
    public void interpolateTo(float x, float y, long duration) {
        inter_start = new Vector2f(getX(), getY());
        inter_target = new Vector2f(x, y);
        inter_timeStart = System.currentTimeMillis();
        inter_duration = duration;
        interpolate = true;
    }

    @Override
    public void setHasLighting(boolean lighting) { }

    @Override
    public void setWidth(float width) { }

    @Override
    public void setHeight(float height) { }

    @Override
    public float getRotation() {
        return 0;
    }

    @Override
    public void setRotation(float rotation) { }

    @Override
    public void setCenterX(float x) {
        setX(x);
    }

    @Override
    public void setCenterY(float y) {
        setY(y);
    }

    @Override
    public void setCenter(float x, float y) {
        setX(x);
        setY(y);
    }

    @Override
    public void setSize(float width, float height) { }
}
