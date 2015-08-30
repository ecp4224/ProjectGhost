package me.eddiep.ghost.common.game;

import me.eddiep.ghost.common.network.packet.MatchEndPacket;
import me.eddiep.ghost.common.network.packet.MatchFoundPacket;
import me.eddiep.ghost.common.network.packet.MatchStatusPacket;
import me.eddiep.ghost.common.network.world.NetworkWorld;
import me.eddiep.ghost.game.match.LiveMatchImpl;
import me.eddiep.ghost.game.match.entities.Entity;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.entities.playable.impl.BaseNetworkPlayer;
import me.eddiep.ghost.game.match.world.timeline.EntitySpawnSnapshot;
import me.eddiep.ghost.game.queue.QueueType;
import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.game.team.Team;
import me.eddiep.ghost.network.Server;
import me.eddiep.ghost.utils.PRunnable;
import me.eddiep.ghost.utils.TimeUtils;
import me.eddiep.ghost.utils.Vector2f;

import java.io.IOException;
import java.util.ArrayList;

public class NetworkMatch extends LiveMatchImpl {
    public static final int MAP_XMIN = 0;
    public static final int MAP_XMAX = 1024;
    public static final int MAP_XMIDDLE = MAP_XMIN + ((MAP_XMAX - MAP_XMIN) / 2);
    public static final int MAP_YMIN = 0;
    public static final int MAP_YMAX = 720;
    public static final int MAP_YMIDDLE = MAP_YMIN + ((MAP_YMAX - MAP_YMIN) / 2);

    public static final Vector2f LOWER_BOUNDS = new Vector2f(MAP_XMIN, MAP_YMIN);
    public static final Vector2f UPPER_BOUNDS = new Vector2f(MAP_XMAX, MAP_YMAX);

    private NetworkWorld networkWorld;
    private ArrayList<Player> disconnectdPlayers = new ArrayList<>();

    public NetworkMatch(Team team1, Team team2, Server server) {
        super(team1, team2, server);
    }

    public NetworkMatch(BaseNetworkPlayer player1, BaseNetworkPlayer player2) {
        super(player1, player2);
    }

    @Override
    protected void onSetup() {
        /*//Here, we will manually spawn all entities for players
        for (Entity e : networkWorld.getEntities()) {
            for (User player : networkWorld.getPlayers()) {
                if (!player.isConnected())
                    continue;

                try {
                    networkWorld.spawnEntityFor(player, EntitySpawnSnapshot.createEvent(e));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }*/
    }

    private void spawnAllEntitiesFor(User n) throws IOException {
        if (!n.isConnected())
            return;

        Entity[] uwotm8 = networkWorld.getEntities().toArray(new Entity[networkWorld.getEntities().size()]);
        for (Entity e : uwotm8) {
            if (e == n)
                continue;

            networkWorld.spawnEntityFor(n, EntitySpawnSnapshot.createEvent(e));
        }
    }

    public void setQueueType(Queues queue) {
        super.queue = queue;
    }

    public void setID(long id) {
        super.id = id;
    }

    public void setWorld(NetworkWorld world) {
        this.networkWorld = world;
        super.world = world;
    }

    @Override
    public void dispose() {
        super.dispose();

        if (disposed)
            return;

        networkWorld = null;
        disconnectdPlayers.clear();
        disconnectdPlayers = null;
    }

    @Override
    protected void onPlayerAdded(PlayableEntity p) {
        if (p instanceof User) {
            User n = (User)p;


            MatchFoundPacket packet = new MatchFoundPacket(n.getClient());
            try {
                packet.writePacket(p.getX(), p.getY());
                //Entities will not be spawned for the player here because the timeline has not started yet
                networkWorld.addPlayer(n);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onMatchEnded() {
        executeOnAllConnected(new PRunnable<User>() {
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
        TimeUtils.executeIn(500, new Runnable() {
            @Override
            public void run() {
                MatchFactory.getCreator().endAndSaveMatch(NetworkMatch.this);
            }
        });
    }

    @Override
    public void setActive(boolean val, final String reason) {
        super.setActive(val, reason);

        executeOnAllConnected(new PRunnable<User>() {
            @Override
            public void run(User p) {
                p.getClient().getPlayer().sendMatchMessage(reason);
            }
        });
    }

    @Override
    public Vector2f getLowerBounds() {
        return LOWER_BOUNDS;
    }

    @Override
    public Vector2f getUpperBounds() {
        return UPPER_BOUNDS;
    }

    public void executeOnAllConnected(PRunnable<User> r) {
        for (User n : networkWorld.getPlayers()) {
            if (!n.isConnected())
                continue;

            r.run(n);
        }
    }

    public void playerReconnected(Player player) throws IOException {
        disconnectdPlayers.remove(player);

        MatchFoundPacket packet = new MatchFoundPacket(player.getClient());
        packet.writePacket(player.getX(), player.getY());

        spawnAllEntitiesFor(player);

        MatchStatusPacket packet2 = new MatchStatusPacket(player.getClient());
        packet2.writePacket(active, lastActiveReason);

        /*if (disconnectdPlayers.size() == 0) {
            setActive(false, "Starting match in 5 seconds..");

            countdownStart = System.currentTimeMillis();
            countdown = true;
            countdownSeconds = 0;
        } else {
            setActive(false, "Player " + disconnectdPlayers.get(0).getUsername() + " disconnected..");
        }*/
    }

    public void playerDisconnected(Player p) {
        if (ended)
            return;

        if (started) {
            disconnectdPlayers.add(p);

            if (queueType().getQueueType() == QueueType.RANKED) {
                if (p.getTeam().getTeamNumber() == team1.getTeamNumber())
                    end(team2);
                else
                    end(team1);

            } else if (entireTeamDisconnected(p.getTeam())) {
                if (p.getTeam().getTeamNumber() == team1.getTeamNumber())
                    end(team2);
                else
                    end(team1);

            }
        }
    }

    private boolean entireTeamDisconnected(Team team) {
        for (PlayableEntity p : team.getTeamMembers()) {
            if (!(p instanceof Player))
                continue;

            if (!disconnectdPlayers.contains(p))
                return false;
        }
        return true;
    }
}
