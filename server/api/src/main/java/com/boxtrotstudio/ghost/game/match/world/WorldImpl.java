package com.boxtrotstudio.ghost.game.match.world;

import com.boxtrotstudio.ghost.game.match.Event;
import com.boxtrotstudio.ghost.game.match.LiveMatch;
import com.boxtrotstudio.ghost.game.match.entities.Entity;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.entities.map.FlagEntity;
import com.boxtrotstudio.ghost.game.match.entities.map.MapEntityFactory;
import com.boxtrotstudio.ghost.game.match.states.ScoreState;
import com.boxtrotstudio.ghost.game.match.world.map.ItemSpawn;
import com.boxtrotstudio.ghost.game.match.world.map.WorldMap;
import com.boxtrotstudio.ghost.game.match.world.physics.Hitbox;
import com.boxtrotstudio.ghost.game.match.world.physics.Physics;
import com.boxtrotstudio.ghost.game.match.world.physics.PhysicsImpl;
import com.boxtrotstudio.ghost.game.match.world.timeline.*;
import com.boxtrotstudio.ghost.game.team.Team;
import com.boxtrotstudio.ghost.network.Server;
import com.boxtrotstudio.ghost.utils.*;
import com.boxtrotstudio.ghost.utils.tick.Tickable;
import com.boxtrotstudio.ghost.utils.tick.TickerPool;
import com.boxtrotstudio.ghost.game.match.world.map.Light;
import com.boxtrotstudio.ghost.utils.tick.Ticker;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private ArrayList<EventSnapshot> events = new ArrayList<>();

    private List<ItemSpawn> itemSpawnPoints;

    private boolean captureTheFlag;
    private FlagEntity team1Flag;
    private FlagEntity team2Flag;

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
    public FlagEntity getTeamFlag(Team team) {
        if (team.getTeamNumber() == 1)
            return team1Flag;
        else
            return team2Flag;
    }

    @Override
    public Vector2f randomLocation(int minx, int miny, int maxx, int maxy) {
        do {
            int x = Global.random(minx, maxx);
            int y = Global.random(miny, maxy);

            final Vector2f point = new Vector2f(x, y);

            boolean test = false;
            if (physics != null) {
                test = physics.foreach(new PFunction<Hitbox, Boolean>() {
                    @Override
                    public Boolean run(Hitbox val) {
                        return val.isPointInside(point);
                    }
                });
            }

            if (!test)
                return point;

        } while (true);
    }

    @Override
    public void onLoad() {
        tickToken = TickerPool.requestTicker(this);

        try {
            map = WorldMap.fromFile(new File("maps", mapName() + ".json"));
        } catch (FileNotFoundException e) {
            System.err.println("No map file found!");
            return;
        }

        if (map == null)
            return;

        physics = new PhysicsImpl();

        for (WorldMap.EntityLocation info : map.getStartingLocations()) {
            Entity entity = MapEntityFactory.createEntity(this, info);
            if (entity == null)
                continue;

            if (entity instanceof FlagEntity) {
                captureTheFlag = true;
                FlagEntity flag = (FlagEntity)entity;
                if (flag.getTeam() == 1)
                    team1Flag = flag;
                else
                    team2Flag = flag;
            }

            spawnEntity(entity);
        }

        if (captureTheFlag) {
            match.setWinCondition(new ScoreState(1)); //Capture the flag is based on score
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
    public boolean isCaptureTheFlag() {
        return captureTheFlag;
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

        if (itemSpawnPoints != null) {
            itemSpawnPoints.clear();
            itemSpawnPoints = null;
        }

        entities.clear();
        toAdd.clear();
        toRemove.clear();
        spawns.clear();
        despawns.clear();
        playableChanges.clear();
        cache.clear();
        ids.clear();

        if (itemSpawnPoints != null)
            itemSpawnPoints.clear();

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
        itemSpawnPoints = null;

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
     * An idle tick is a tick that occurs while this {@link World} is idle. Only the match
     * gets a tick and nothing else. All entities are considered frozen and the timeline is halted.
     */
    protected void idleTick() {
        match.tick();
    }

    /**
     * An active tick is a tick that occurs while this {@link World} is active. All entities
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

        if (itemSpawnPoints != null) {
            for (ItemSpawn spawn : itemSpawnPoints) {
                spawn.tick(match);
            }
        }

        match.tick();

        timeline.tick();
        onTimelineTick();

        if (match.isMatchActive() && match.getTimeElapsed() - lastEntityUpdate >= Constants.UPDATE_STATE_INTERVAL) {
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
        EntitySpawnSnapshot[] spawnSnapshots = spawns.toArray(new EntitySpawnSnapshot[spawns.size()]);
        EntityDespawnSnapshot[] despawnSnapshots = despawns.toArray(new EntityDespawnSnapshot[despawns.size()]);
        PlayableSnapshot[] playableSnapshots = playableChanges.toArray(new PlayableSnapshot[playableChanges.size()]);
        EventSnapshot[] eventSnapshots = events.toArray(new EventSnapshot[events.size()]);

        spawns.clear();
        despawns.clear();
        playableChanges.clear();
        events.clear();

        return WorldSnapshot.takeSnapshot(this, spawnSnapshots, despawnSnapshots, playableSnapshots, eventSnapshots);
    }

    @Override
    public void playableUpdated(PlayableEntity entity) {
        playableChanges.add(PlayableSnapshot.createEvent(entity));
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

    @Override
    public List<ItemSpawn> getItemSpawns() {
        return Collections.unmodifiableList(itemSpawnPoints);
    }

    @Override
    public void clearItemSpawns() {
        itemSpawnPoints.clear();
    }

    @Override
    public void addItemSpawn(ItemSpawn spawn) {
        if (itemSpawnPoints == null) {
            itemSpawnPoints = new LinkedList<>();
            if (match != null)
                match.disableItems();
        }

        itemSpawnPoints.add(spawn);
    }

    @Override
    public void triggerEvent(Event event, Entity cause, double direction) {
        events.add(EventSnapshot.createEvent(event, cause, direction));
    }
}
