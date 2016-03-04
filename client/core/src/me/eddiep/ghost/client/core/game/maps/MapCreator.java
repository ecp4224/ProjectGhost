package me.eddiep.ghost.client.core.game.maps;

import me.eddiep.ghost.client.handlers.scenes.SpriteScene;
import org.jetbrains.annotations.NotNull;

public interface MapCreator {
    @NotNull
    MapCreator[] MAPS = new MapCreator[] {
            new Factory()
    };

    void construct(@NotNull SpriteScene world);

    @NotNull
    String name();
}
