package me.eddiep.ghost.game.match.world;

import me.eddiep.ghost.game.match.entities.Entity;
import me.eddiep.ghost.game.match.LiveMatch;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.world.physics.Physics;
import me.eddiep.ghost.game.match.world.timeline.*;
import me.eddiep.ghost.network.Client;
import me.eddiep.ghost.utils.annotations.InternalOnly;

import java.io.IOException;
import java.util.List;

public interface World {

    void spawnEntity(Entity entity);

    void despawnEntity(Entity entity);

    void spawnParticle(ParticleEffect effect, int duration, int size, float x, float y, double rotation);

    boolean isInWorld(Entity entity);

    void tick();

    void onLoad();

    void onFinishLoad();

    List<Entity> getEntities();

    WorldSnapshot takeSnapshot();

    LiveMatch getMatch();

    void requestEntityUpdate();

    Timeline getTimeline();

    void updateClient(Client client) throws IOException;

    void updateClient(Client client, WorldSnapshot snapshot) throws IOException;

    void playableUpdated(PlayableEntity updated);

    boolean isActive();

    boolean isIdle();

    boolean isDisposed();

    boolean isPaused();

    void dispose();

    void pause();

    void idle();

    void activate();

    <T extends Entity> T getEntity(short id);

    @InternalOnly
    EntitySpawnSnapshot[] getSpawns();

    @InternalOnly
    EntityDespawnSnapshot[] getDespawns();

    @InternalOnly
    PlayableSnapshot[] getPlayableChanges();

    Physics getPhysics();
}
