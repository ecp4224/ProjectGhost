package me.eddiep.ghost.game.match.world;

import me.eddiep.ghost.game.match.entities.Entity;
import me.eddiep.ghost.game.match.LiveMatch;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.entities.map.WallEntity;
import me.eddiep.ghost.game.match.world.map.WorldMap;
import me.eddiep.ghost.game.match.world.timeline.*;
import me.eddiep.ghost.network.Server;
import me.eddiep.ghost.utils.Tickable;
import me.eddiep.ghost.utils.TimeUtils;
import me.eddiep.ghost.utils.Vector2f;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static me.eddiep.ghost.utils.Constants.UPDATE_STATE_INTERVAL;

public abstract class WorldImpl implements World, Tickable {
    private static final long TICK_RATE = 16;
    protected ArrayList<Entity> entities = new ArrayList<>();
    private ArrayList<Entity> toAdd = new ArrayList<>();
    private ArrayList<Entity> toRemove = new ArrayList<>();
    private Map<Short, Entity> cache = new HashMap<>();
    private ArrayList<Short> ids = new ArrayList<>();

    private List<Tickable> toTick = Collections.synchronizedList(new ArrayList<Tickable>());
    private List<Tickable> tempTick = new ArrayList<>();
    private boolean ticking = false;
    private Thread tickThread;

    private ArrayList<EntitySpawnSnapshot> spawns = new ArrayList<>();
    private ArrayList<EntityDespawnSnapshot> despawns = new ArrayList<>();
    private ArrayList<PlayableSnapshot> playableChanges = new ArrayList<>();
    protected Timeline timeline;
    protected LiveMatch match;
    private AtomicBoolean isTicking = new AtomicBoolean(false);
    protected long lastEntityUpdate;
    protected Server server;
    protected WorldMap map;
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
        tickThread = new Thread(TICK_RUNNABLE);
        tickThread.start();

        try {
            map = WorldMap.fromFile(new File(mapName()));
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
                case -127:
                    entity = new WallEntity();
                    break;

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
        if (Thread.currentThread() != tickThread) {
            System.err.println("Despawning in an unsafe way!");
            new Throwable().printStackTrace();
        }

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

        if (shouldRequestTick()) {
            executeNextTick(this);
            /*server.executeNextTick(new Runnable() {
                @Override
                public void run() {
                    tick();
                }
            });*/
        } else { //This world no longer wants ticks so it's not needed
            TimeUtils.executeIn(500, new Runnable() {
                @Override
                public void run() {
                    dispose();
                }
            });
        }
    }

    @Override
    public void dispose() {
        if (disposed)
            return;

        disposed = true;

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

        tickThread.interrupt();
        try {
            tickThread.join();
        } catch (InterruptedException e) { }
        toTick.clear();
        tempTick.clear();
        tickThread = null;
        toTick = null;
        tempTick = null;
    }

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

    private final Runnable TICK_RUNNABLE = new Runnable() {
        @Override
        public void run() {
            boolean set = false;

            while (!disposed) {
                if (!set && getMatch().getID() != 0) {
                    Thread.currentThread().setName("Match " + getMatch().getID());
                    set = true;
                }

                try {
                    handleTickLogic();
                } catch (Throwable t) {
                    System.err.println("Error ticking!");
                    t.printStackTrace();
                }

                if (disposed)
                    return;

                try {
                    Thread.sleep(TICK_RATE);
                } catch (InterruptedException ignored) {
                }
            }
        }
    };

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
}
