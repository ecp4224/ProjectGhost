package me.eddiep.ghost.client.core;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import me.eddiep.ghost.client.utils.annotations.InternalOnly;

import java.util.ArrayList;

public class Entity extends Sprite implements Drawable {
    private float z;
    private boolean hasLoaded = false;
    private short id;

    private ArrayList<Entity> children = new ArrayList<Entity>();
    private ArrayList<Entity> parents = new ArrayList<Entity>();

    public Entity(Sprite sprite, short id) {
        super(sprite);

        this.id = id;
    }

    public Entity(short id) {
        super();

        this.id = id;
    }

    public short getID() {
        return id;
    }

    @InternalOnly
    public void load() {
        onLoad();
        if (!hasLoaded)
            throw new IllegalStateException("super.onLoad() was not invoked!");
    }

    protected void onLoad() {
        hasLoaded = true;
    }

    @InternalOnly
    public void unload() {
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
        super.setX(x);

        for (Entity c : children) {
            c.setX(x);
        }
    }

    @Override
    public void setY(float y) {
        super.setY(y);

        for (Entity c : children) {
            c.setY(y);
        }
    }

    public void attach(Entity e) {
        children.add(e);
        e.addParent(this);
    }

    public void deattach(Entity e) {
        children.remove(e);
        e.removeParent(this);
    }

    private void addParent(Entity e) {
        parents.add(e);
    }

    private void removeParent(Entity e) {
        parents.remove(e);
    }
}
