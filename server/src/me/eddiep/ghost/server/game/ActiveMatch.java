package me.eddiep.ghost.server.game;

import me.eddiep.ghost.server.Main;
import me.eddiep.ghost.server.TcpUdpServer;
import me.eddiep.ghost.server.game.entities.NetworkEntity;
import me.eddiep.ghost.server.game.entities.PlayableEntity;
import me.eddiep.ghost.server.game.entities.TypeableEntity;
import me.eddiep.ghost.server.game.entities.playable.impl.Player;
import me.eddiep.ghost.server.game.queue.QueueType;
import me.eddiep.ghost.server.game.queue.Queues;
import me.eddiep.ghost.server.game.ranking.Glicko2;
import me.eddiep.ghost.server.game.stats.MatchHistory;
import me.eddiep.ghost.server.game.team.OfflineTeam;
import me.eddiep.ghost.server.game.team.Team;
import me.eddiep.ghost.server.game.util.Vector2f;
import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.network.packet.impl.*;
import me.eddiep.ghost.server.utils.PRunnable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.eddiep.ghost.server.utils.Constants.UPDATE_STATE_INTERVAL;

public class ActiveMatch implements Match {
    public static final int MAP_XMIN = 0;
    public static final int MAP_XMAX = 1024;
    public static final int MAP_XMIDDLE = MAP_XMIN + ((MAP_XMAX - MAP_XMIN) / 2);
    public static final int MAP_YMIN = 0;
    public static final int MAP_YMAX = 720;
    public static final int MAP_YMIDDLE = MAP_YMIN + ((MAP_YMAX - MAP_YMIN) / 2);
    private static final int COUNTDOWN_LIMIT = 5;

    private ArrayList<Entity> entities = new ArrayList<>();
    private ArrayList<NetworkEntity> networkEntities = new ArrayList<>();
    private ArrayList<Short> ids = new ArrayList<>();
    private ArrayList<NetworkEntity> disconnectdPlayers = new ArrayList<>();
    private Team team1;
    private Team team2;
    private TcpUdpServer server;
    private boolean started;
    private boolean active;

    private long lastEntityUpdate;

    private long matchID = -1;
    private int winningTeam = -1;
    private long matchStarted = -1, matchEnded = -1;

    private boolean countdown = false;
    private long countdownStart;
    private int countdownSeconds;

    private long timeStarted;
    private Queues queue;

    public ActiveMatch(Team team1, Team team2, TcpUdpServer server) {
        this.team1 = team1;
        this.team2 = team2;
        this.server = server;
    }

    public ActiveMatch(Player player1, Player player2) {
        this(new Team(1, player1), new Team(2, player2), player1.getClient().getServer());
    }

    void setQueueType(Queues type) {
        this.queue = type;
    }

    void setID(long id) {
        this.matchID = id;
    }

    public Team getTeam1() {
        return team1;
    }

    public Team getTeam2() {
        return team2;
    }

    @Override
    public long getID() {
        return matchID;
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

    private void start() {
        started = true;

        timeStarted = System.currentTimeMillis();

        executeOnAllPlayers(new PRunnable<PlayableEntity>() {
            @Override
            public void run(PlayableEntity p) {
                p.setReady(false);
                p.prepareForMatch();
            }
        });
        matchStarted = System.currentTimeMillis();
        setActive(true, "Match started");
    }

    public Team getTeamFor(PlayableEntity player) {
        if (team1.isAlly(player))
            return team1;
        else if (team2.isAlly(player))
            return team2;
        return null;
    }

    private void setID(Entity entity) {
        short id = 0;
        do {
            id++;
        } while (ids.contains(id));

        entity.setID(id);
        ids.add(entity.getID());
    }

    public void tick() {
        if (!started) {
            //READY STATE
            if (team1.isTeamReady() && team2.isTeamReady()) {
                start();
            }
        }

        if (countdown) {
            //COUNTDOWN WHEN MATCH HAS BEEN PAUSED
            if (System.currentTimeMillis() - countdownStart >= 1000 * (countdownSeconds + 1)) {
                countdownSeconds++;
                if (countdownSeconds < COUNTDOWN_LIMIT) {
                    setActive(false, "Starting match in " + (COUNTDOWN_LIMIT - countdownSeconds) + " seconds..");
                } else {
                    setActive(true, "Match started");
                    countdown = false;
                }
            }
        }

        if (active) {
            //STUFF TO DO WHILE MATCH IS ACTIVE
            Entity[] toTick = entities.toArray(new Entity[entities.size()]);
            for (Entity e : toTick) {
                e.tick();
            }

            //Send entity state packets
            if (getTimeElapsed() - lastEntityUpdate >= UPDATE_STATE_INTERVAL) {
                lastEntityUpdate = getTimeElapsed();
                updateEntityState();
            }

            //Check winning state
            if (team1.isTeamDead() && !team2.isTeamDead()) {
                end(team2);
            } else if (!team1.isTeamDead() && team2.isTeamDead()) {
                end(team1);
            } else if (team1.isTeamDead()) { //team2.isTeamDead() is always true at this point in the elseif
                end(null);
            }
        }

        if (ended) {
            //CLEAN UP MATCH
            if (System.currentTimeMillis() - matchEnded >= 5000) {
                executeOnAllPlayers(new PRunnable<PlayableEntity>() {
                    @Override
                    public void run(PlayableEntity p) {
                        p.resetLives();
                        ((Entity)p).setID((short)-1);
                        p.setMatch(null);
                    }
                });
                executeOnAllConnected(new PRunnable<NetworkEntity>() {
                    @Override
                    public void run(NetworkEntity p) {
                        boolean won = (p instanceof PlayableEntity && getWinningTeam().isAlly((PlayableEntity) p));

                        MatchEndPacket packet = new MatchEndPacket(p.getClient());
                        try {
                            packet.writePacket(won, getID());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                MatchFactory.endAndSaveMatch(this);
                return;
            }
        }

        //Request next tick
        server.executeNextTick(new Runnable() {
            @Override
            public void run() {
                tick();
            }
        });
    }

    public void updateEntityState() {
        List<Entity> buffer = new ArrayList<>();
        for (NetworkEntity n : networkEntities) {
            if (!n.isConnected())
                continue;
            buffer.clear();

            if (n instanceof PlayableEntity) {
                PlayableEntity np = (PlayableEntity)n;
                for (Entity e : entities) {
                    if (e instanceof PlayableEntity) {
                        PlayableEntity ep = (PlayableEntity)e;

                        if (ep.shouldSendUpdatesTo(np))
                            buffer.add(e);
                    } else {
                        buffer.add(e); //We're not concerned about if they can see this entity or not
                    }
                }
            } else {
                buffer.addAll(entities); //This network entity is not playing, so they can see everything
            }

            BulkEntityStatePacket packet = new BulkEntityStatePacket(n.getClient());
            try {
                packet.writePacket(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void spawnAllEntitiesFor(NetworkEntity n) throws IOException {
        if (!n.isConnected())
            return;

        for (Entity e : entities) {
            if (e == n)
                continue;

            spawnEntityFor(n, e);
        }
    }

    private void despawnEntityFor(NetworkEntity n, Entity e) throws IOException {
        DespawnEntityPacket packet = new DespawnEntityPacket(n.getClient());
        packet.writePacket(e);
    }

    private void spawnEntityFor(NetworkEntity n, Entity e) throws IOException {
        if (e.getID() == -1)
            setID(e);

        SpawnEntityPacket packet = new SpawnEntityPacket(n.getClient());
        byte type;
        if (n instanceof PlayableEntity) {
            PlayableEntity np = (PlayableEntity)n;
            if (e instanceof PlayableEntity) {
                PlayableEntity ep = (PlayableEntity)e;

                if (np.getTeam().isAlly(ep)) {
                    type = 0;
                } else {
                    type = 1;
                }
            } else if (e instanceof TypeableEntity) {
                type = ((TypeableEntity)e).getType();
            } else {
                return;
            }
        } else {
            if (e instanceof PlayableEntity) {
                PlayableEntity ep = (PlayableEntity)e;

                if (ep.getTeam().getTeamNumber() == team1.getTeamNumber())
                    type = 0;
                else
                    type = 1;
            } else if (e instanceof TypeableEntity) {
                type = ((TypeableEntity)e).getType();
            } else {
                return;
            }
        }

        packet.writePacket(e, type);
    }

    public void spawnEntity(Entity e) throws IOException {
        if (e.getID() == -1)
            setID(e);

        entities.add(e);

        for (NetworkEntity n : networkEntities) {
            if (!n.isConnected())
                continue;

            spawnEntityFor(n, e);
        }
    }

    public List<Entity> getEntities() {
        return Collections.unmodifiableList(entities);
    }

    public void despawnEntity(Entity e) throws IOException {
        entities.remove(e);
        ids.remove((Short)e.getID());

        for (NetworkEntity n : networkEntities) {
            if (!n.isConnected())
                continue;

            despawnEntityFor(n, e);
        }
    }

    /**
     * This method should be invoked when a PlayableEntity updated and the changes are relevant to the
     * {@link me.eddiep.ghost.server.network.packet.impl.PlayerStatePacket}
     * This method should be invoked to update all {@link me.eddiep.ghost.server.game.entities.NetworkEntity}
     * @param entity The entity that updated
     */
    public void playableUpdated(PlayableEntity entity) {
        for (NetworkEntity n : networkEntities) {
            if (!n.isConnected())
                continue;

            PlayerStatePacket packet = new PlayerStatePacket(n.getClient());
            try {
                packet.writePacket(entity);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setup() throws IOException {
        for (PlayableEntity p : team1.getTeamMembers()) {
            float p1X = (float)Main.random(MAP_XMIN, MAP_XMIDDLE);
            float p1Y = (float)Main.random(MAP_YMIN, MAP_YMAX);

            p.setPosition(new Vector2f(p1X, p1Y));
            p.setVelocity(0f, 0f);

            if (p instanceof NetworkEntity) {
                NetworkEntity n = (NetworkEntity)p;

                MatchFoundPacket packet = new MatchFoundPacket(n.getClient());
                packet.writePacket(p1X, p1Y);

                networkEntities.add(n);
            }

            p.setMatch(this);
            p.setVisible(true);
            entities.add(p);
        }

        for (PlayableEntity p : team2.getTeamMembers()) {
            float p1X = (float)Main.random(MAP_XMIDDLE, MAP_XMAX);
            float p1Y = (float)Main.random(MAP_YMIN, MAP_YMAX);

            p.setPosition(new Vector2f(p1X, p1Y));
            p.setVelocity(0f, 0f);

            if (p instanceof NetworkEntity) {
                NetworkEntity n = (NetworkEntity)p;

                MatchFoundPacket packet = new MatchFoundPacket(n.getClient());
                packet.writePacket(p1X, p1Y);

                networkEntities.add(n);
            }

            p.setMatch(this);
            p.setVisible(true);
            entities.add(p);
        }

        for (NetworkEntity n : networkEntities) {
            spawnAllEntitiesFor(n);
        }

        server.executeNextTick(new Runnable() {
            @Override
            public void run() {
                tick();
            }
        });
    }

    public long getTimeElapsed() {
        return System.currentTimeMillis() - timeStarted;
    }

    public boolean isMatchActive() {
        return started && active && !ended;
    }

    public boolean hasMatchStarted() {
        return started;
    }

    public boolean hasMatchEnded() {
        return ended;
    }

    public void setActive(boolean state, final String reason) {
        this.active = state;

        executeOnAllConnected(new PRunnable<NetworkEntity>() {
            @Override
            public void run(NetworkEntity p) {
                MatchStatusPacket packet = new MatchStatusPacket(p.getClient());
                try {
                    packet.writePacket(active, reason);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void executeOnAllConnected(PRunnable<NetworkEntity> r) {
        for (NetworkEntity n : networkEntities) {
            if (!n.isConnected())
                continue;

            r.run(n);
        }
    }

    public void executeOnAllPlayers(PRunnable<PlayableEntity> r) {
        for (PlayableEntity p : team1.getTeamMembers()) {
                r.run(p);
        }
        for (PlayableEntity p : team2.getTeamMembers()) {
            r.run(p);
        }
    }

    private boolean entireTeamDisconnected(Team team) {
        for (PlayableEntity p : team.getTeamMembers()) {
            if (!disconnectdPlayers.contains(p))
                return false;
        }
        return true;
    }

    public void playerDisconnected(NetworkEntity player) {
        if (ended)
            return;
        if (!(player instanceof PlayableEntity))
            return;

        PlayableEntity pe = (PlayableEntity)player;

        if (started) {
            if (queueType().getQueueType() == QueueType.RANKED) {
                if (pe.getTeam().getTeamNumber() == team1.getTeamNumber())
                    end(team2);
                else
                    end(team1);

                return;
            } else if (entireTeamDisconnected(pe.getTeam())) {
                if (pe.getTeam().getTeamNumber() == team1.getTeamNumber())
                    end(team2);
                else
                    end(team1);

                return;
            }

            disconnectdPlayers.add(player);
        }
    }

    public void playerReconnected(NetworkEntity player) throws IOException {
        disconnectdPlayers.remove(player);

        MatchFoundPacket packet = new MatchFoundPacket(player.getClient());
        packet.writePacket(player.getX(), player.getY());

        spawnAllEntitiesFor(player);

        /*if (disconnectdPlayers.size() == 0) {
            setActive(false, "Starting match in 5 seconds..");

            countdownStart = System.currentTimeMillis();
            countdown = true;
            countdownSeconds = 0;
        } else {
            setActive(false, "Player " + disconnectdPlayers.get(0).getUsername() + " disconnected..");
        }*/
    }

    private boolean ended = false;
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
                p.setVelocity(0f, 0f);
                p.setVisible(true);
            }
        });

        updateEntityState();

        if (winners == null) {
            setActive(false, "Draw!");
        } else {
            setActive(false, winners.getTeamMembers()[0].getName() + " wins!");
        }

        if (queueType().getQueueType() == QueueType.RANKED) {
            Glicko2.getInstance().completeMatch(this);
        }
    }

    public MatchHistory matchHistory() {
        return new MatchHistory(this);
    }

    private boolean disposed;
    public void dispose() {
        if (disposed)
            return;

        disposed = true;
        team1 = null;
        team2 = null;
        entities.clear();
        ids.clear();
        disconnectdPlayers.clear();

        entities = null;
        ids = null;
        disconnectdPlayers = null;
        server = null;
    }

    public void addSpectator(Client client) {

    }
}