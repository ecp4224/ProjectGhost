package me.eddiep.ghost.client.core.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import me.eddiep.ghost.client.core.render.scene.impl.SpriteScene;

public interface Drawable {
    void draw(SpriteBatch batch);

    void load();

    void unload();

    Blend blendMode();

    boolean hasLighting();

    int getZIndex();

    SpriteScene getParentScene();

    void setParentScene(SpriteScene scene);
}
