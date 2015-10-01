package me.eddiep.ghost.client.core;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;

public class DrawableSprite implements Drawable, Attachable {
    private Sprite sprite;

    private ArrayList<Attachable> children = new ArrayList<Attachable>();
    private ArrayList<Attachable> parents = new ArrayList<Attachable>();

    public DrawableSprite(Sprite sprite) {
        this.sprite = sprite;
    }

    @Override
    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

    @Override
    public void load() {

    }

    @Override
    public void unload() {

    }

    @Override
    public void attach(Attachable attach) {
        children.add(attach);
        attach.addParent(this);
    }

    @Override
    public void deattach(Attachable attach) {
        children.remove(attach);
        attach.removeParent(this);
    }

    @Override
    public float getX() {
        return sprite.getX();
    }

    @Override
    public float getY() {
        return sprite.getY();
    }

    @Override
    public void setX(float x) {
        sprite.setX(x);

        for (Attachable a : children) {
            a.setX(x);
        }
    }

    @Override
    public void setY(float y) {
        sprite.setY(y);

        for (Attachable a : children) {
            a.setY(y);
        }
    }

    @Override
    public void addParent(Attachable parent) {
        parents.add(parent);
    }

    @Override
    public void removeParent(Attachable parent) {
        parents.remove(parent);
    }
}
