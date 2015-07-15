package me.eddiep.ghost.game.match;

import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.entities.playable.impl.BaseNetworkPlayer;
import me.eddiep.ghost.game.match.world.World;
import me.eddiep.ghost.game.queue.QueueType;
import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.game.ranking.Glicko2;
import me.eddiep.ghost.game.stats.MatchHistory;
import me.eddiep.ghost.game.team.OfflineTeam;
import me.eddiep.ghost.game.team.Team;
import me.eddiep.ghost.network.Server;
import me.eddiep.ghost.utils.Global;
import me.eddiep.ghost.utils.PRunnable;
import me.eddiep.ghost.utils.Vector2f;

import static me.eddiep.ghost.utils.Constants.COUNTDOWN_LIMIT;

public abstract class LiveMatchImpl implements LiveMatch {
    protected Team team1, team2;
    protected Server server;
    protected World world;
    protected int winningTeam, losingTeam;
    protected boolean started;
    protected boolean countdown;
    protected long countdownStart;
    protected int countdownSeconds;
    protected long timeStarted;
    protected long matchStarted;
    protected boolean active;
    protected String lastActiveReason;
    protected boolean ended;
    protected long matchEnded;
    protected Queues queue;
    protected long id;

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

        int map_xmin = (int) getLowerBounds().x, map_xmax = (int) getUpperBounds().x, map_xmiddle = map_xmin + ((map_xmax - map_xmin) / 2);
        int map_ymin = (int) getLowerBounds().y, map_ymax = (int) getUpperBounds().y, map_ymiddle = map_ymin + ((map_ymax - map_ymin) / 2);

        for (PlayableEntity p : team1.getTeamMembers()) {
            float p1X = (float) Global.random(map_xmin, map_xmiddle);
            float p1Y = (float) Global.random(map_ymin, map_ymax);

            p.setPosition(new Vector2f(p1X, p1Y));
            p.setVelocity(0f, 0f);

            onPlayerAdded(p);

            p.setMatch(this);
            p.setVisible(true);
            world.spawnEntity(p);
        }

        for (PlayableEntity p : team2.getTeamMembers()) {
            float p1X = (float) Global.random(map_xmiddle, map_xmax);
            float p1Y = (float) Global.random(map_ymin, map_ymax);

            p.setPosition(new Vector2f(p1X, p1Y));
            p.setVelocity(0f, 0f);

            /*if (p instanceof User) {
                User n = (User)p;

                MatchFoundPacket packet = new MatchFoundPacket(n.getClient());
                packet.writePacket(p1X, p1Y);

                networkEntities.add(n);
            }*/
            onPlayerAdded(p);

            p.setMatch(this);
            p.setVisible(true);
            world.spawnEntity(p);
        }

        /*for (User n : networkEntities) {
            spawnAllEntitiesFor(n);
        }*/

        /*server.executeNextTick(new Runnable() {
            @Override
            public void run() {
                tick();
            }
        });*/

        onSetup();

        server.executeNextTick(new Runnable() {
            @Override
            public void run() {
                world.tick();
            }
        });
    }

    protected abstract void onSetup();

    protected abstract void onPlayerAdded(PlayableEntity playableEntity);

    protected abstract void onMatchEnded();

    @Override
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
                        p.setID((short) -1);
                        p.setMatch(null);
                    }
                });
                /*executeOnAllConnected(new PRunnable<User>() {
                    @Override
                    public void run(User p) {
                        boolean won = (p instanceof PlayableEntity && getWinningTeam().isAlly((PlayableEntity) p));

                        MatchEndPacket packet = new MatchEndPacket(p.getClient());
                        try {
                            packet.writePacket(won, getID());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                MatchFactory.endAndSaveMatch(this);*/
                onMatchEnded();
            }
        }
    }

    protected void end(Team winners) {
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
            setActive(false, "Draw!");
        } else {
            setActive(false, winners.getTeamMembers()[0].getName() + " wins!");
        }

        if (queueType().getQueueType() == QueueType.RANKED) {
            Glicko2.getInstance().completeMatch(this);
        }
    }

    protected void start() {
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



    protected void setActive(boolean state, final String reason) {
        this.active = state;
        this.lastActiveReason = reason;
    }

    public void executeOnAllPlayers(PRunnable<PlayableEntity> r) {
        for (PlayableEntity p : team1.getTeamMembers()) {
            r.run(p);
        }
        for (PlayableEntity p : team2.getTeamMembers()) {
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
}
