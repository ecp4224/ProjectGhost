package com.boxtrotstudio.ghost.client.core.game.maps;

import com.boxtrotstudio.ghost.client.handlers.scenes.SpriteScene;
import org.jetbrains.annotations.NotNull;

public interface MapCreator {
    @NotNull
    MapCreator[] MAPS = new MapCreator[] {
    };

    void construct(@NotNull SpriteScene world);

    @NotNull
    String name();
}
