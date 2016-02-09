package me.eddiep.ghost.game.match;

import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.entities.playable.impl.BaseNetworkPlayer;
import me.eddiep.ghost.game.match.item.*;
import me.eddiep.ghost.game.match.world.World;
import me.eddiep.ghost.game.match.world.physics.Hitbox;
import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.game.stats.MatchHistory;
import me.eddiep.ghost.game.team.OfflineTeam;
import me.eddiep.ghost.game.team.Team;
import me.eddiep.ghost.network.Server;
import me.eddiep.ghost.utils.*;
import me.eddiep.ghost.utils.tick.Tickable;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import static me.eddiep.ghost.utils.Constants.AVERAGE_MATCH_TIME;
import static me.eddiep.ghost.utils.Constants.READY_TIMEOUT;

public abstract class LiveMatchImpl implements LiveMatch {
    protected Team team1, team2;
    protected Server server;
    protected World world;
    protected int winningTeam, losingTeam;
    protected boolean started;
    protected long timeStarted;
    protected long matchStarted;
    protected boolean active;
    protected String lastActiveReason;
    protected boolean ended;
    protected long matchEnded;
    protected Queues queue;
    protected long id;

    protected long readyWaitStart;

    protected boolean shouldSpawnItems = true;
    protected int maxItems = 0;
    protected long nextItemTime = 0;
    protected int itemsSpawned = 0;
    protected ArrayList<Item> items = new ArrayList<>();


    protected boolean timed = false;
    protected boolean overtime = false;
    protected int matchDuration = 150; //2:30
    protected long matchTimedEnd;

    protected Runnable countdownComplete;
    protected boolean countdown;
    protected long countdownStart;
    protected int countdownSeconds;
    protected int countdownLimit;
    protected String countdownMessage;

    protected final Object tickLock = new Object();

    public static final Class[] ITEMS = new Class[] {
            EmpItem.class,
            FireRateItem.class,
            HealthItem.class,
            //InvisibleItem.class,
            JamItem.class,
            ShieldItem.class,
            SpeedItem.class
    };

    public LiveMatchImpl(Team team1, Team team2, Server server) {
        this.team1 = team1;
        this.team2 = team2;
        this.server = server;
    }

    public LiveMatchImpl(BaseNetworkPlayer player1, BaseNetworkPlayer player2) {
        this(new Team(1, player1), new Team(2, player2), player1.getClient().getServer());
    }

    @Override
    public void setup() {
        world.onLoad();

        onSetup();

        int map_xmin = (int) getLowerBounds().x, map_xmax = (int) getUpperBounds().x, map_xmiddle = map_xmin + ((map_xmax - map_xmin) / 2);
        int map_ymin = (int) getLowerBounds().y, map_ymax = (int) getUpperBounds().y, map_ymiddle = map_ymin + ((map_ymax - map_ymin) / 2);

        for (PlayableEntity p : team1.getTeamMembers()) {
            Vector2f start = randomLocation(map_xmin, map_ymin, map_xmiddle, map_ymax);

            p.setPosition(start);
            p.setVelocity(0f, 0f);

            p.setMatch(this);
            p.setVisible(true);
            p.setCanChangeAbility(false); //We don't want players changing mid-game

            onPlayerAdded(p);

            world.spawnEntity(p);
        }

        for (PlayableEntity p : team2.getTeamMembers()) {
            Vector2f start = randomLocation(map_xmiddle, map_ymin, map_xmax, map_ymax);

            p.setPosition(start);
            p.setVelocity(0f, 0f);

            p.setMatch(this);
            p.setVisible(true);
            p.setCanChangeAbility(false); //We don't want players changing mid-game

            onPlayerAdded(p);

            world.spawnEntity(p);
        }

        world.onFinishLoad();

        world.activate();

        setActive(false, "Press space to ready up!");

        world.executeNextTick(new Tickable() {
            @Override
            public void tick() {
                world.tick();
            }
        });

        readyWaitStart = System.currentTimeMillis();
    }

    protected Vector2f randomLocation(int minx, int miny, int maxx, int maxy) {
        do {
            int x = Global.random(minx, maxx);
            int y = Global.random(miny, maxy);

            final Vector2f point = new Vector2f(x, y);

            boolean test = world.getPhysics().foreach(new PFunction<Hitbox, Boolean>() {
                @Override
                public Boolean run(Hitbox val) {
                    return val.isPointInside(point);
                }
            });

            if (!test)
                return point;

        } while (true);
    }

    protected Item createItem(Class class_) {
        try {
            return (Item) class_.getConstructor(LiveMatch.class).newInstance(this);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected abstract void onSetup();

    protected abstract void onPlayerAdded(PlayableEntity playableEntity);

    protected abstract void onMatchEnded();

    public void onReady(PlayableEntity e) {
        e.setVisible(true);
        world.requestEntityUpdate();
    }

    @Override
    public void tick() {
        synchronized (tickLock) {
            if (!started) {
                //READY STATE
                if (team1.isTeamReady() && team2.isTeamReady()) {
                    start();
                } else {
                    if (System.currentTimeMillis() - readyWaitStart >= READY_TIMEOUT) {
                        cancelGame();
                    }
                }
            }

            if (countdown && countdownComplete != null) {
                //COUNTDOWN WHEN MATCH HAS BEEN PAUSED
                if (System.currentTimeMillis() - countdownStart >= 1000 * (countdownSeconds + 1)) {
                    countdownSeconds++;
                    if (countdownSeconds < countdownLimit) {
                        setActive(false, countdownMessage.replace("%t", "" + (countdownLimit - countdownSeconds)));
                    } else {
                        countdownComplete.run();
                        countdown = false;
                    }
                }
            }

            if (active) {
                if (timed && !overtime) {
                    long timeLeft = matchTimedEnd - System.currentTimeMillis();
                    setActive(true, formatTime(timeLeft));

                    if (timeLeft <= 0) {
                        if (team1.totalLives() > team2.totalLives()) {
                            end(team1);
                        } else if (team2.totalLives() > team1.totalLives()) {
                            end(team2);
                        } else {
                            setActive(true, "OVERTIME");

                            for (PlayableEntity p : getPlayers()) {
                                if (!p.isDead()) {
                                    p.setLives((byte) 1);
                                }
                            }
                        }
                    }
                }

                //Tick Items
                Item[] checkItems = items.toArray(new Item[items.size()]);
                for (Item i : checkItems) {
                    if (!i.isActive()) { //Check for collision and handle collision related stuff
                        for (PlayableEntity e : team1.getTeamMembers()) {
                            i.checkIntersection(e);
                        }
                        for (PlayableEntity e : team2.getTeamMembers()) {
                            i.checkIntersection(e);
                        }
                    }

                    i.tick();
                }

                //Check winning state
                if (team1.isTeamDead() && !team2.isTeamDead()) {
                    end(team2);
                } else if (!team1.isTeamDead() && team2.isTeamDead()) {
                    end(team1);
                } else if (team1.isTeamDead()) { //team2.isTeamDead() is always true at this point in the elseif
                    end(null);
                }

                //Spawn items
                if (shouldSpawnItems && nextItemTime != 0 && System.currentTimeMillis() - nextItemTime >= 0) {
                    int ranIndex = Global.random(0, ITEMS.length);
                    spawnItem(createItem(ITEMS[ranIndex]));

                    if (++itemsSpawned < maxItems) {
                        calculateNextItemTime();
                    } else {
                        nextItemTime = 0;
                    }
                }
            }

            if (ended) {
                //CLEAN UP MATCH
                if (System.currentTimeMillis() - matchEnded >= 5000) {
                    executeOnAllPlayers(new PRunnable<PlayableEntity>() {
                        @Override
                        public void run(PlayableEntity p) {
                            p.resetLives();
                            p.setID((short) -1);
                            p.setMatch(null);
                            p.setCanChangeAbility(true); //Reset this value
                        }
                    });

                    onMatchEnded();

                    world.pause();
                }
            }
        }
    }

    private void cancelGame() {
        matchStarted = matchEnded = System.currentTimeMillis();

        ended = true;
        started = true;

        winningTeam = -1;

        executeOnAllPlayers(new PRunnable<PlayableEntity>() {
            @Override
            public void run(PlayableEntity p) {
                p.setVelocity(0f, 0f);
                p.setVisible(true);
            }
        });

        world.requestEntityUpdate();
        world.idle();

        setActive(false, "Game canceled! Not enough players connected");
    }

    @Override
    public void disableItems() {
        shouldSpawnItems = false;
    }

    @Override
    public void enableItems() {
        shouldSpawnItems = true;
    }

    @Override
    public int getPlayerCount() {
        return getTeam1().getTeamLength() + getTeam2().getTeamLength();
    }

    @Override
    public PlayableEntity[] getPlayers() {
        return ArrayHelper.combine(team1.getTeamMembers(), team2.getTeamMembers());
    }

    @Override
    public void spawnItem(Item item) {
        items.add(item);
        world.spawnEntity(item.getEntity());
    }

    @Override
    public void despawnItem(Item item) {
        items.remove(item);
    }

    protected void calculateNextItemTime() {
        int div = maxItems + Global.random(-3, 3);
        if (div == 0) {
            div = 1;
        }

        nextItemTime = AVERAGE_MATCH_TIME / div;

        if (nextItemTime < 0) {
            nextItemTime = 5_000;
        }

        nextItemTime += System.currentTimeMillis();
    }

    public void end(Team winners) {
        if (ended)
            return;

        matchEnded = System.currentTimeMillis();

        ended = true;
        if (winners != null) {
            winningTeam = winners.getTeamNumber();
            winners.onWin(this);
            getLosingTeam().onLose(this);
        } else {
            winningTeam = -1;
        }

        executeOnAllPlayers(new PRunnable<PlayableEntity>() {
            @Override
            public void run(PlayableEntity p) {
                p.freeze(); p.setVisible(true);
            }
        });

        world.requestEntityUpdate();

        announceWinners(winners);
    }

    protected void announceWinners(Team winners) {
        if (winners == null) {
            setActive(false, "Draw!", false);
        } else {
            setActive(false, winners.getTeamName() + " wins!", false);
        }
    }

    public void forfeit(Team winners) {
        if (ended)
            return;

        matchEnded = System.currentTimeMillis();

        ended = true;
        if (winners != null) {
            winningTeam = winners.getTeamNumber();
            winners.onWin(this);
            getLosingTeam().onLose(this);
        } else {
            winningTeam = -1;
        }

        executeOnAllPlayers(new PRunnable<PlayableEntity>() {
            @Override
            public void run(PlayableEntity p) {
                p.setVelocity(0f, 0f);
                p.setVisible(true);
            }
        });

        world.requestEntityUpdate();

        if (winners == null) {
            setActive(false, "Draw!", false);
        } else {
            setActive(false, winners.getTeamName() + " wins by forfeit!", false);
        }
    }

    protected boolean disposed;
    public void dispose() {
        if (disposed)
            return;

        disposed = true;

        items.clear();

        team1.dispose();
        team1 = null;

        team2.dispose();
        team2 = null;

        server = null;
        world = null;
        items = null;
    }

    public void start() {
        if (started)
            return;

        started = true;

        maxItems = Global.random(getPlayerCount(), 4 * getPlayerCount());
        calculateNextItemTime();

        startCountdown(5, "Game will start in %t", new Runnable() {
            @Override
            public void run() {
                timeStarted = System.currentTimeMillis();

                executeOnAllPlayers(new PRunnable<PlayableEntity>() {
                    @Override
                    public void run(PlayableEntity p) {
                        p.setVisible(false);
                        p.setReady(false);
                        p.prepareForMatch();
                        if (p.getLives() == 0) //If at this point lives is still 0
                            p.setLives((byte) 3); //Set it to default
                    }
                });

                matchStarted = System.currentTimeMillis();

                if (timed) {
                    matchTimedEnd = matchStarted + (matchDuration * 1000);
                    setActive(true, formatTime(matchDuration * 1000));
                } else {
                    setActive(true, "");
                }
            }
        });
    }

    protected void setActive(boolean state, final String reason) {
        setActive(state, reason, !state);
    }

    protected void setActive(boolean state, final String reason, boolean setIdle) {
        this.active = state;
        this.lastActiveReason = reason;

        if (setIdle)
            world.idle();
        else
            world.activate();
    }

    public void executeOnAllPlayers(PRunnable<PlayableEntity> r) {
        for (PlayableEntity p : team1.getTeamMembers()) {
            if (p.getMatch() == null)
                continue;

            r.run(p);
        }
        for (PlayableEntity p : team2.getTeamMembers()) {
            if (p.getMatch() == null)
                continue;

            r.run(p);
        }
    }

    @Override
    public Server getServer() {
        return server;
    }

    @Override
    public Team getTeam1() {
        return team1;
    }

    @Override
    public Team getTeam2() {
        return team2;
    }
    public Team getWinningTeam() {
        if (team1.getTeamNumber() == winningTeam)
            return team1;
        else if (team2.getTeamNumber() == winningTeam)
            return team2;
        return null;
    }

    public Team getLosingTeam() {
        if (team1.getTeamNumber() == winningTeam)
            return team2;
        else if (team2.getTeamNumber() == winningTeam)
            return team1;
        return null;
    }

    @Override
    public Team getTeamFor(PlayableEntity player) {
        if (team1.isAlly(player))
            return team1;
        else if (team2.isAlly(player))
            return team2;
        return null;
    }

    @Override
    public long getTimeElapsed() {
        return System.currentTimeMillis() - timeStarted;
    }

    @Override
    public boolean isMatchActive() {
        return active;
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public void playableUpdated(PlayableEntity updated) {
        world.playableUpdated(updated);
    }

    @Override
    public boolean hasMatchStarted() {
        return started;
    }

    @Override
    public boolean hasMatchEnded() {
        return ended;
    }

    @Override
    public MatchHistory matchHistory() {
        return new MatchHistory(this);
    }

    @Override
    public long getID() {
        return id;
    }

    @Override
    public OfflineTeam team1() {
        return team1.offlineTeam();
    }

    @Override
    public OfflineTeam team2() {
        return team2.offlineTeam();
    }

    @Override
    public OfflineTeam winningTeam() {
        if (team1.getTeamNumber() == winningTeam)
            return team1.offlineTeam();
        else if (team2.getTeamNumber() == winningTeam)
            return team2.offlineTeam();
        return null;
    }

    @Override
    public OfflineTeam losingTeam() {
        if (team1.getTeamNumber() == winningTeam)
            return team2.offlineTeam();
        else if (team2.getTeamNumber() == winningTeam)
            return team1.offlineTeam();
        return null;
    }

    @Override
    public long getMatchStarted() {
        return matchStarted;
    }

    @Override
    public long getMatchEnded() {
        return matchEnded;
    }

    @Override
    public Queues queueType() {
        return queue;
    }

    public boolean isMatchTimed() {
        return timed;
    }

    public String formatTime(long milliSeconds) {
        long seconds = (milliSeconds % 60000) / 1000;
        long minutes = milliSeconds / 60000;

        return (minutes < 10 ? "0" + minutes : "" + minutes) + ":" + (seconds < 10 ? "0" + seconds : "" + seconds);
    }

    public boolean shouldSpawnItems() {
        return shouldSpawnItems;
    }

    public void shouldSpawnItems(boolean shouldSpawnItems) {
        this.shouldSpawnItems = shouldSpawnItems;
    }

    public void startCountdown(int seconds, String message, Runnable runnable) {
        this.countdownComplete = runnable;
        this.countdownLimit = seconds;
        this.countdownStart = System.currentTimeMillis();
        this.countdown = true;
        this.countdownMessage = message;
    }

    public void cancelCountdown() {
        this.countdown = false;
    }

    public void setTimed(boolean isTimed, int time) {
        this.timed = isTimed;
        this.matchDuration = time;
    }
}
