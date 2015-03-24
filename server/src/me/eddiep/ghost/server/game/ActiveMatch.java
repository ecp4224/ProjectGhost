package me.eddiep.ghost.server.game;

import me.eddiep.ghost.server.Main;
import me.eddiep.ghost.server.TcpUdpServer;
import me.eddiep.ghost.server.game.entities.OfflineTeam;
import me.eddiep.ghost.server.game.entities.Player;
import me.eddiep.ghost.server.game.entities.Team;
import me.eddiep.ghost.server.game.queue.QueueType;
import me.eddiep.ghost.server.game.queue.Queues;
import me.eddiep.ghost.server.game.rating.Glicko2;
import me.eddiep.ghost.server.game.util.Vector2f;
import me.eddiep.ghost.server.network.packet.impl.MatchEndPacket;
import me.eddiep.ghost.server.network.packet.impl.MatchFoundPacket;
import me.eddiep.ghost.server.network.packet.impl.MatchStatusPacket;
import me.eddiep.ghost.server.utils.PRunnable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActiveMatch implements Match {
    public static final int MAP_XMIN = -504;
    public static final int MAP_XMAX = 504;
    public static final int MAP_XMIDDLE = MAP_XMIN + ((MAP_XMAX - MAP_XMIN) / 2);
    public static final int MAP_YMIN = -350;
    public static final int MAP_YMAX = 350;
    public static final int MAP_YMIDDLE = MAP_YMIN + ((MAP_YMAX - MAP_YMIN) / 2);
    private static final int COUNTDOWN_LIMIT = 5;

    private ArrayList<Entity> entities = new ArrayList<>();
    private ArrayList<Short> ids = new ArrayList<>();
    private ArrayList<Player> disconnectdPlayers = new ArrayList<>();
    private Team team1;
    private Team team2;
    private TcpUdpServer server;
    private boolean started;
    private boolean active;

    private int winningTeam = -1, matchID = -1;
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

    void setID(int id) {
        this.matchID = id;
    }

    public Team getTeam1() {
        return team1;
    }

    public Team getTeam2() {
        return team2;
    }

    @Override
    public int getID() {
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

        executeOnAllConnectedPlayers(new PRunnable<Player>() {
            @Override
            public void run(Player p) {
                p.setReady(false);
                p.oldVisibleState = true;
                p.setVisible(false);
                p.resetUpdateTimer();
            }
        });
        matchStarted = System.currentTimeMillis();
        setActive(true, "Match started");
    }

    public Team getTeamFor(Player player) {
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
            if (team1.isTeamReady() && team2.isTeamReady()) {
                start();
            }
        }

        if (countdown) {
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
            Entity[] toTick = entities.toArray(new Entity[entities.size()]);
            for (Entity e : toTick) {
                e.tick();
            }

            if (team1.isTeamDead() && !team2.isTeamDead()) {
                end(team2);
            } else if (!team1.isTeamDead() && team2.isTeamDead()) {
                end(team1);
            } else if (team1.isTeamDead()) { //team2.isTeamDead() is always true at this point in the elseif
                end(null);
            }
        }

        if (ended) {
            if (System.currentTimeMillis() - matchEnded >= 5000) {
                executeOnAllConnectedPlayers(new PRunnable<Player>() {
                    @Override
                    public void run(Player p) {
                        p.resetLives();
                        ((Entity)p).setID((short) -1);
                        p.setMatch(null);
                        MatchEndPacket packet = new MatchEndPacket(p.getClient());
                        try {
                            packet.writePacket(getWinningTeam().isAlly(p), getID());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                MatchFactory.endAndSaveMatch(this);
                return;
            }
        }

        server.executeNextTick(new Runnable() {
            @Override
            public void run() {
                tick();
            }
        });
    }

    private void spawnPlayersFor(Player p) throws IOException {
        for (Player toSpawn : team1.getTeamMembers()) {
            if (p == toSpawn)
                continue;

            if (toSpawn.getID() == -1)
                setID(toSpawn);

            p.spawnEntity(toSpawn);
        }

        for (Player toSpawn : team2.getTeamMembers()) {
            if (p == toSpawn)
                continue;

            if (toSpawn.getID() == -1)
                setID(toSpawn);

            p.spawnEntity(toSpawn);
        }
    }

    public void spawnEntity(Entity e) throws IOException {
        if (e.getID() == -1)
            setID(e);

        entities.add(e);

        for (Player p : team1.getTeamMembers()) {
            if (p == e)
                continue;

            p.spawnEntity(e);
        }

        for (Player p : team2.getTeamMembers()) {
            if (p == e)
                continue;

            p.spawnEntity(e);
        }
    }

    public List<Entity> getEntities() {
        return Collections.unmodifiableList(entities);
    }

    public void despawnEntity(Entity e) throws IOException {
        entities.remove(e);
        ids.remove((Short)e.getID());

        for (Player p : team1.getTeamMembers()) {
            p.despawnEntity(e);
        }

        for (Player p : team2.getTeamMembers()) {
            p.despawnEntity(e);
        }
    }

    public void setup() throws IOException {
        for (Player p : team1.getTeamMembers()) {
            float p1X = (float)Main.random(MAP_XMIN, MAP_XMIDDLE);
            float p1Y = (float)Main.random(MAP_YMIN, MAP_YMAX);

            p.setPosition(new Vector2f(p1X, p1Y));
            p.setVelocity(0f, 0f);

            MatchFoundPacket packet = new MatchFoundPacket(p.getClient());
            packet.writePacket(p1X, p1Y);

            p.setMatch(this);
            p.setVisible(true);
            entities.add(p);
        }

        for (Player p : team2.getTeamMembers()) {
            float p1X = (float)Main.random(MAP_XMIDDLE, MAP_XMAX);
            float p1Y = (float)Main.random(MAP_YMIN, MAP_YMAX);

            p.setPosition(new Vector2f(p1X, p1Y));
            p.setVelocity(0f, 0f);

            MatchFoundPacket packet = new MatchFoundPacket(p.getClient());
            packet.writePacket(p1X, p1Y);

            p.setMatch(this);
            p.setVisible(true);
            entities.add(p);
        }

        for (Player p : team1.getTeamMembers()) {
            spawnPlayersFor(p);
        }

        for (Player p : team2.getTeamMembers()) {
            spawnPlayersFor(p);
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

        executeOnAllConnectedPlayers(new PRunnable<Player>() {
            @Override
            public void run(Player p) {
                MatchStatusPacket packet = new MatchStatusPacket(p.getClient());
                try {
                    packet.writePacket(active, reason);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void executeOnAllConnectedPlayers(PRunnable<Player> r) {
        executeOnAllPlayers(r, true);
    }

    public void executeOnAllPlayers(PRunnable<Player> r, boolean requiresConnection) {
        for (Player p : team1.getTeamMembers()) {
            if (p.isUDPConnected())
                r.run(p);
        }
        for (Player p : team2.getTeamMembers()) {
            if (p.isUDPConnected())
                r.run(p);
        }
    }

    public void playerDisconnected(Player player) {
        if (ended)
            return;

        setActive(false, "Player " + player.getUsername() + " disconnected..");

        disconnectdPlayers.add(player);
    }

    public void playerReconnected(Player player) throws IOException {
        disconnectdPlayers.remove(player);

        MatchFoundPacket packet = new MatchFoundPacket(player.getClient());
        packet.writePacket(player.getX(), player.getY());

        spawnPlayersFor(player);

        if (disconnectdPlayers.size() == 0) {
            setActive(false, "Starting match in 5 seconds..");

            countdownStart = System.currentTimeMillis();
            countdown = true;
            countdownSeconds = 0;
        } else {
            setActive(false, "Player " + disconnectdPlayers.get(0).getUsername() + " disconnected..");
        }
    }

    private boolean ended = false;
    public void end(Team winners) {
        if (ended)
            return;

        ended = true;
        if (winners != null) {
            winningTeam = winners.getTeamNumber();
            winners.onWin(this);
            getLosingTeam().onLose(this);
        } else {
            winningTeam = -1;
        }

        matchEnded = System.currentTimeMillis();
        executeOnAllConnectedPlayers(new PRunnable<Player>() {
            @Override
            public void run(Player p) {
                p.setVelocity(0f, 0f);
                p.setVisible(true);
                try {
                    p.updateState();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        if (winners == null) {
            setActive(false, "Draw!");
        } else {
            setActive(false, winners.getTeamMembers()[0].getUsername() + " wins!");
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

}