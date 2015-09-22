package me.eddiep.ghost.game.match.world;

import me.eddiep.ghost.game.match.entities.Entity;
import me.eddiep.ghost.game.match.LiveMatch;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.entities.map.MirrorEntity;
import me.eddiep.ghost.game.match.entities.map.WallEntity;
import me.eddiep.ghost.game.match.world.map.Light;
import me.eddiep.ghost.game.match.world.map.WorldMap;
import me.eddiep.ghost.game.match.world.physics.Physics;
import me.eddiep.ghost.game.match.world.physics.PhysicsImpl;
import me.eddiep.ghost.game.match.world.timeline.*;
import me.eddiep.ghost.network.Server;
import me.eddiep.ghost.utils.CancelToken;
import me.eddiep.ghost.utils.tick.Tickable;
import me.eddiep.ghost.utils.TimeUtils;
import me.eddiep.ghost.utils.Vector2f;
import me.eddiep.ghost.utils.tick.Ticker;
import me.eddiep.ghost.utils.tick.TickerPool;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static me.eddiep.ghost.utils.Constants.UPDATE_STATE_INTERVAL;

public abstract class WorldImpl implements World, Tickable, Ticker {
    //Entities and lights
    protected ArrayList<Entity> entities = new ArrayList<>(); //List of entities
    protected ArrayList<Light> lights = new ArrayList<>(); //List of lights

    //Cache for entities
    private ArrayList<Entity> toAdd = new ArrayList<>(); //Entities that were added during a tick
    private ArrayList<Entity> toRemove = new ArrayList<>(); //Entities that were removed during a tick
    private Map<Short, Entity> cache = new HashMap<>(); //Cache of entities by ID
    private ArrayList<Short> ids = new ArrayList<>(); //A list of currently used IDs

    //Tick cycle items
    private final List<Tickable> toTick = Collections.synchronizedList(new ArrayList<Tickable>()); //Items to tick
    private List<Tickable> tempTick = new ArrayList<>(); //Buffer of Tickables to add at the end of the tick cycle
    private boolean ticking = false; //Whether we are currently ticking
    private CancelToken tickToken; //A canceltoken to stop ticking

    //Timeline buffer
    private ArrayList<EntitySpawnSnapshot> spawns = new ArrayList<>();
    private ArrayList<EntityDespawnSnapshot> despawns = new ArrayList<>();
    private ArrayList<PlayableSnapshot> playableChanges = new ArrayList<>();

    protected Timeline timeline;
    protected LiveMatch match;
    private AtomicBoolean isTicking = new AtomicBoolean(false);
    protected long lastEntityUpdate;
    protected Server server;
    protected WorldMap map;
    protected Physics physics;
    private boolean active, idle, disposed;

    public WorldImpl(LiveMatch match) {
        this.match = match;
        this.timeline = new Timeline(this);
        this.server = match.getServer();
    }

    private void setID(Entity entity) {
        short id = 0;
        do {
            id++;
        } while (ids.contains(id));

        entity.setID(id);
        ids.add(entity.getID());
    }

    @Override
    public void onLoad() {
        tickToken = TickerPool.requestTicker(this);

        physics = new PhysicsImpl();

        try {
            map = WorldMap.fromFile(new File("maps", mapName() + ".json"));
        } catch (FileNotFoundException e) {
            System.err.println("No map file found!");
            return;
        }

        if (map == null)
            return;

        for (WorldMap.EntityLocations e : map.getStartingLocations()) {
            Entity entity;
            switch (e.getId()) {
                //TODO Put stuff here
                case 81:
                    entity = new MirrorEntity();
                    break;
                case 80:
                    entity = new WallEntity();
                    break;
                case -1:
                    //light
                    float   x = e.getX(),
                            y = e.getY(),
                            radius = Float.parseFloat(e.getExtra("radius")),
                            intensity = Float.parseFloat("intensity");
                    Color color = new Color (
                            Integer.parseInt(e.getExtra("red")),
                            Integer.parseInt(e.getExtra("green")),
                            Integer.parseInt(e.getExtra("blue"))
                    );

                    Light light = new Light(x, y, radius, intensity, color);
                    this.spawnLight(light);
                    continue;
                default:
                    entity = null;
            }
            if (entity == null)
                continue;

            entity.setPosition(new Vector2f(e.getX(), e.getY()));
            entity.setRotation(e.getRotation());

            spawnEntity(entity);
        }
    }

    /**
     * Execute a {@link java.lang.Runnable} next tick
     * @param runnable The runnable to execute
     */
    @Override
    public final void executeNextTick(Tickable runnable) {
        if (runnable == null) {
            System.err.println("Given null tickable! Please investage this problem!");
            System.err.println(Arrays.toString(Thread.currentThread().getStackTrace()));
            return;
        }
        if (!ticking) {
            toTick.add(runnable);
        } else {
            tempTick.add(runnable);
        }
    }

    public WorldMap getMap() {
        return map;
    }

    public abstract String mapName();

    @Override
    public void onFinishLoad() {
        //Here we create the setup tick on the timeline
        timeline.tick();
        onTimelineTick();

        //And request an entity update to send the new setup tick
        requestEntityUpdate();
    }

    @Override
    public void spawnEntity(Entity entity) {
        if (entity.getID() == -1)
            setID(entity);

        if (isTicking.get()) {
            toAdd.add(entity);
        } else {
            entities.add(entity);
        }

        spawns.add(EntitySpawnSnapshot.createEvent(entity));
        cache.put(entity.getID(), entity);

        entity.setWorld(this);
    }

    @Override
    public void despawnEntity(Entity entity) {
        if (isTicking.get()) {
            toRemove.add(entity);
        } else {
            entities.remove(entity);
        }

        despawns.add(EntityDespawnSnapshot.createEvent(entity));
        cache.remove(entity.getID());

        entity.setWorld(null);
    }

    @Override
    public boolean isInWorld(Entity entity) {
        return entities.contains(entity);
    }

    @Override
    public void handleTick() {
        if (getMatch().getWorld() == null) {
            tickToken.cancel();
            return;
        }

        try {
            handleTickLogic();
        } catch (Throwable t) {
            System.err.println("Error ticking!");
            t.printStackTrace();
        }

        if (disposed) {
            tickToken.cancel();
        }
    }

    @Override
    public void tick() {
        if (disposed) {
            System.err.println("[SERVER] Ticked invoked on disposed world! Ignoring..");
            return;
        }

        if (active) {
            activeTick();
        } else if (idle) {
            idleTick();
        }

        if (!shouldRequestTick()) { //This world no longer wants ticks so it's not needed
            TimeUtils.executeIn(1000, new Runnable() {
                @Override
                public void run() {
                    dispose();
                }
            });
        } else {
            executeNextTick(this);
        }
    }

    @Override
    public void dispose() {
        if (disposed)
            return;

        disposed = true;
        long matchDuration = match.getMatchEnded() - match.getMatchStarted();

        for (Entity entity : entities) {
            cache.remove(entity.getID());

            entity.setWorld(null);
        }

        //System.out.println("[SERVER] Disposing world for match " + match.getID());

        entities.clear();
        toAdd.clear();
        toRemove.clear();
        spawns.clear();
        despawns.clear();
        playableChanges.clear();
        cache.clear();
        ids.clear();

        match.dispose();

        server = null;
        match = null;
        entities = null;
        toAdd = null;
        toRemove = null;
        spawns = null;
        despawns = null;
        playableChanges = null;
        cache = null;
        ids = null;
        isTicking = null;

        timeline.dispose();
        timeline = null; //Big memory leak o.o

        if (map != null) {
            map.dispose();
            map = null;
        }

        tickToken.cancel();
        toTick.clear();
        tempTick.clear();

        if (matchDuration >= 3 * 60 * 1000 && System.currentTimeMillis() - lastGCRequest > 120000) {
            System.gc();
            lastGCRequest = System.currentTimeMillis();
        }
    }
    private static long lastGCRequest;

    @Override
    public <T extends Entity> T getEntity(short id) {
        return (T) cache.get(id);
    }

    /**
     * An idle tick is a tick that occurs while this {@link me.eddiep.ghost.game.match.world.World} is idle. Only the match
     * gets a tick and nothing else. All entities are considered frozen and the timeline is halted.
     */
    protected void idleTick() {
        match.tick();
    }

    /**
     * An active tick is a tick that occurs while this {@link me.eddiep.ghost.game.match.world.World} is active. All entities
     * get ticks and the current LiveMatch get a tick as well.
     */
    protected void activeTick() {
        isTicking.set(true);

        Iterator<Entity> entityIterator = entities.iterator();
        while (entityIterator.hasNext()) {
            Entity e = entityIterator.next();
            if (e.getWorld() == null)
                entityIterator.remove();
            else if (e.isRequestingTicks())
                e.tick();
        }

        isTicking.set(false);

        entities.addAll(toAdd);
        toAdd.clear();

        entities.removeAll(toRemove);
        toRemove.clear();

        match.tick();

        timeline.tick();
        onTimelineTick();

        if (match.isMatchActive() && match.getTimeElapsed() - lastEntityUpdate >= UPDATE_STATE_INTERVAL) {
            lastEntityUpdate = match.getTimeElapsed();
            requestEntityUpdate();
        }
    }

    protected abstract void onTimelineTick();

    protected boolean shouldRequestTick() {
        return !(match.hasMatchEnded() && System.currentTimeMillis() - match.getMatchEnded() > 10000);
    }

    @Override
    public void spawnLight(Light light) {
        this.lights.add(light);
    }

    @Override
    public List<Light> getLights() {
        return Collections.unmodifiableList(lights);
    }

    @Override
    public List<Entity> getEntities() {
        return Collections.unmodifiableList(entities);
    }

    @Override
    public WorldSnapshot takeSnapshot() {
        return WorldSnapshot.takeSnapshot(this);
    }

    @Override
    public LiveMatch getMatch() {
        return match;
    }

    @Override
    public abstract void requestEntityUpdate();

    @Override
    public Timeline getTimeline() {
        return timeline;
    }

    @Override
    public EntitySpawnSnapshot[] getSpawns() {
        if (spawns.size() == 0)
            return null;

        EntitySpawnSnapshot[] temp = spawns.toArray(new EntitySpawnSnapshot[spawns.size()]);
        spawns.clear();

        return temp;
    }

    @Override
    public EntityDespawnSnapshot[] getDespawns() {
        if (despawns.size() == 0)
            return null;

        EntityDespawnSnapshot[] temp = despawns.toArray(new EntityDespawnSnapshot[despawns.size()]);
        despawns.clear();

        return temp;
    }

    public void playableUpdated(PlayableEntity entity) {
        playableChanges.add(PlayableSnapshot.createEvent(entity));
    }

    @Override
    public PlayableSnapshot[] getPlayableChanges() {
        if (playableChanges.size() == 0)
            return null;

        PlayableSnapshot[] temp = playableChanges.toArray(new PlayableSnapshot[playableChanges.size()]);
        playableChanges.clear();

        return temp;
    }

    @Override
    public final boolean isActive() {
        return active;
    }

    @Override
    public final boolean isIdle() {
        return idle;
    }

    @Override
    public final boolean isDisposed() {
        return disposed;
    }

    @Override
    public final boolean isPaused() {
        return !idle && !active && !disposed;
    }

    @Override
    public final void idle() {
        idle = true;
        active = false;
    }

    @Override
    public final void activate() {
        idle = false;
        active = true;
    }

    @Override
    public final void pause() {
        idle = false;
        active = false;
    }

    @Override
    public void spawnParticle(ParticleEffect effect, int duration, int size, float x, float y, double rotation) {
        spawns.add(EntitySpawnSnapshot.createParticleEvent(effect, duration, size, x, y, rotation));
    }
    private long tickLength;
    private void handleTickLogic() {
        long s = System.nanoTime();
        synchronized (toTick) {
            Iterator<Tickable> runnableIterator = toTick.iterator();

            ticking = true;
            while (runnableIterator.hasNext()) {
                if (disposed)
                    return;

                Tickable r = runnableIterator.next();
                if (r != null)
                    r.tick();
                else {
                    System.err.println("Null tickable found in tick loop! Please investigate this..(" + toTick.size() + ")");
                }
                runnableIterator.remove();
            }
        }
        ticking = false;
        toTick.addAll(tempTick);
        tempTick.clear();
        tickLength = (System.nanoTime() - s);
    }

    public long getTickCycleLength() {
        return tickLength;
    }

    @Override
    public Physics getPhysics() {
        return physics;
    }

    @Override
    public WorldMap getWorldMap() {
        return map;
    }
}
