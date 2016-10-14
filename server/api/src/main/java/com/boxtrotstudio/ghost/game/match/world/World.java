package com.boxtrotstudio.ghost.game.match.world;

import com.boxtrotstudio.ghost.game.match.Event;
import com.boxtrotstudio.ghost.game.match.entities.map.FlagEntity;
import com.boxtrotstudio.ghost.game.match.world.timeline.Timeline;
import com.boxtrotstudio.ghost.game.match.LiveMatch;
import com.boxtrotstudio.ghost.game.match.entities.Entity;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.world.map.ItemSpawn;
import com.boxtrotstudio.ghost.game.match.world.map.Light;
import com.boxtrotstudio.ghost.game.match.world.map.WorldMap;
import com.boxtrotstudio.ghost.game.match.world.physics.Physics;
import com.boxtrotstudio.ghost.game.match.world.timeline.WorldSnapshot;
import com.boxtrotstudio.ghost.game.team.Team;
import com.boxtrotstudio.ghost.utils.Vector2f;
import com.boxtrotstudio.ghost.utils.tick.Tickable;

import java.util.List;

public interface World {

    void spawnEntity(Entity entity);

    void despawnEntity(Entity entity);

    /**
     * Spawn a particle effect at the specified location
     * @param effect The effect to play
     * @param duration The duration of the effect
     * @param size The size of the effect
     * @param x The x location of the effect
     * @param y The y location of the effect
     * @param rotation The rotation
     * @deprecated This function will be replaced by the {@link World#triggerEvent(Event, Entity, double)}. The client
     * should handle particle effects
     */
    @Deprecated
    void spawnParticle(ParticleEffect effect, int duration, int size, float x, float y, double rotation);

    void spawnLight(Light light);

    boolean isInWorld(Entity entity);

    Vector2f randomLocation(int xmin, int ymin, int xmax, int ymax);

    FlagEntity getTeamFlag(Team team);

    void tick();

    void onLoad();

    void onFinishLoad();

    void triggerEvent(Event event, Entity cause, double direction);

    boolean isCaptureTheFlag();

    List<Entity> getEntities();

    List<Light> getLights();

    WorldSnapshot takeSnapshot();

    LiveMatch getMatch();

    void requestEntityUpdate();

    Timeline getTimeline();

    void playableUpdated(PlayableEntity updated);

    boolean isActive();

    boolean isIdle();

    boolean isDisposed();

    boolean isPaused();

    void dispose();

    void pause();

    void idle();

    void activate();

    void executeNextTick(Tickable tick);

    <T extends Entity> T getEntity(short id);

    Physics getPhysics();

    WorldMap getWorldMap();

    List<ItemSpawn> getItemSpawns();

    void clearItemSpawns();

    void addItemSpawn(ItemSpawn spawn);

    /**
     * Spawn an entity for a certain player. This entity will not receive updates nor will it
     * be included in {@link Timeline}.
     * <br></br>
     * <br></br>
     * It is up to the implementation what this function does. It should be used to display a static entity
     * to a player.
     * @param player The player who should receive this entity
     * @param entity The entity to spawn for the player. This entity will not receive ticks nor will it be included
     *               in the {@link Timeline}
     */
    void spawnEntityFor(PlayableEntity player, Entity entity);

    /**
     * Despawn an entity for a certain player. If this entity was spawned using the {@link World#spawnEntity(Entity)}
     * method, then this entity will continue to receive ticks and will continue to be included in the {@link Timeline}.
     * If you wish to properly despawn an entity, then use the {@link World#despawnEntity(Entity)} method. This despawn
     * event will not be included in the {@link Timeline}
     * <br></br>
     * <br></br>
     * It is up to the implementation what this function does. It should be used to remove a static entity from a
     * player's view that was spawned using the {@link World#spawnEntityFor(PlayableEntity, Entity)} method. Using this
     * method to despawn a "global" entity (An entity spawned using {@link World#spawnEntity(Entity)}) may have an
     * undesired effect and may cause unstable behavior
     * @param player The player who should receive this entity
     * @param entity The entity to despawn for the player.
     */
    void despawnEntityFor(PlayableEntity player, Entity entity); //TODO Maybe don't allow despawning of global entities
}
