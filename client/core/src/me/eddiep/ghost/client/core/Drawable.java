package me.eddiep.ghost.client.core;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface Drawable {
    void draw(SpriteBatch batch);

    void load();

    void unload();

    Blend blendMode();

    boolean hasLighting();
}
