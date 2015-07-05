package me.eddiep.ghost.game.match.world;

import me.eddiep.ghost.game.match.entities.Entity;
import me.eddiep.ghost.game.match.LiveMatch;
import me.eddiep.ghost.game.match.world.timeline.*;
import me.eddiep.ghost.network.Client;
import me.eddiep.ghost.utils.annotations.InternalOnly;

import java.io.IOException;
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

    void updateClient(Client client) throws IOException;

    void updateClient(Client client, WorldSnapshot snapshot) throws IOException;

    @InternalOnly
    EntitySpawnSnapshot[] getSpawns();

    @InternalOnly
    EntityDespawnSnapshot[] getDespawns();
}
