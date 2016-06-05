package com.boxtrotstudio.ghost.client.core.game.sprites.effects;


import com.boxtrotstudio.ghost.client.handlers.scenes.SpriteScene;

public interface Effect {
    Effect[] EFFECTS = {
            new ChargeEffect(),
            new LineEffect(),
            new CircleEffect()
    };

    void begin(int duration, int size, float x, float y, double rotation, SpriteScene world);
}
