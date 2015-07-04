package me.eddiep.ghost.game.world;

import me.eddiep.ghost.game.Entity;
import me.eddiep.ghost.game.LiveMatch;
import me.eddiep.ghost.game.world.timeline.Timeline;
import me.eddiep.ghost.game.world.timeline.TimelineCursor;
import me.eddiep.ghost.game.world.timeline.WorldSnapshot;

import java.util.List;

public interface World {

    void spawnEntity(Entity entity);

    void despawnEntity(Entity entity);

    boolean isInWorld(Entity entity);

    void tick();

    void onLoad();

    List<Entity> getEntities();

    WorldSnapshot takeSnapshot();

    LiveMatch getMatch();

    void requestEntityUpdate();

    Timeline getTimeline();

    TimelineCursor getSpectatorCursor();
}
