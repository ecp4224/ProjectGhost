package com.boxtrotstudio.ghost.client.core.game.events;

import com.boxtrotstudio.ghost.client.core.game.Entity;
import com.boxtrotstudio.ghost.client.handlers.scenes.SpriteScene;
import org.jetbrains.annotations.NotNull;

public interface Event {
    short getID();

    void trigger(@NotNull Entity cause, double direction, @NotNull SpriteScene world);
}
