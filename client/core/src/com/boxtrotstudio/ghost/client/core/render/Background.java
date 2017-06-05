package com.boxtrotstudio.ghost.client.core.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.boxtrotstudio.ghost.client.Ghost;

public class Background {

    public static Drawable withColor(Color color) {
        return new TextureRegionDrawable(new Sprite(Ghost.ASSETS.get("sprites/wall.png", Texture.class))).tint(color);
    }
}
