package me.eddiep.ghost.client.core;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import me.eddiep.ghost.client.utils.annotations.InternalOnly;

public class Entity extends Sprite implements Drawable {
    private float z;
    private boolean hasLoaded = false;

    public Entity(Sprite sprite) {
        super(sprite);
    }

    public Entity() {
        super();
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
}
