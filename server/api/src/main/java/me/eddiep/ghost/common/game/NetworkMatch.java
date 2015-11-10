package me.eddiep.ghost.common.game;

import me.eddiep.ghost.common.network.packet.*;
import me.eddiep.ghost.common.network.world.NetworkWorld;
import me.eddiep.ghost.game.match.LiveMatchImpl;
import me.eddiep.ghost.game.match.entities.Entity;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.entities.playable.impl.BaseNetworkPlayer;
import me.eddiep.ghost.game.match.world.map.Light;
import me.eddiep.ghost.game.match.world.timeline.EntitySpawnSnapshot;
import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.game.team.Team;
import me.eddiep.ghost.network.Server;
import me.eddiep.ghost.utils.PRunnable;
import me.eddiep.ghost.utils.TimeUtils;
import me.eddiep.ghost.utils.Vector2f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    public ArrayList<Player> disconnectdPlayers = new ArrayList<>();

    public NetworkMatch(Team team1, Team team2, Server server) {
        super(team1, team2, server);
    }

    public NetworkMatch(BaseNetworkPlayer player1, BaseNetworkPlayer player2) {
        super(player1, player2);
    }

    @Override
    protected void onSetup() { }

    @Override
    public void onReady(PlayableEntity e) {
        super.onReady(e);

        if (e instanceof Player) {
            disconnectdPlayers.remove(e);
        }
    }

    @Override
    public void tick() {
        synchronized (tickLock) {
            if (started && entireTeamDisconnected(team1) && entireTeamDisconnected(team2)) {
                end(null);
            }
        }
        super.tick();
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

        List<Light> lights = networkWorld.getLights();
        for (short i = 0; i < lights.size(); i++) {
            SpawnLightPacket light = new SpawnLightPacket(n.getClient());
            light.writePacket(lights.get(i), i);
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
        if (disposed)
            return;

        super.dispose();

        networkWorld = null;
        disconnectdPlayers.clear();
        disconnectdPlayers = null;
    }

    @Override
    protected void onPlayerAdded(PlayableEntity p) {
        if (p instanceof Player) {
            disconnectdPlayers.add((Player) p); //All players start off disconnected
        }

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
                boolean won;
                if (getWinningTeam() == null)
                    won = false;
                else
                    won = (p instanceof PlayableEntity && getWinningTeam().isAlly((PlayableEntity) p));

                MatchEndPacket packet = new MatchEndPacket(p.getClient());
                try {
                    packet.writePacket(won, getID());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        MatchFactory.getCreator().endAndSaveMatch(NetworkMatch.this);
    }

    @Override
    public void setActive(boolean val, final String reason, boolean setIdle) {
        super.setActive(val, reason, setIdle);

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

    public void playerDisconnected(final Player p) {
        if (ended)
            return;

        if (started) {
            synchronized (tickLock) { //Prevent ticking while changing states
                disconnectdPlayers.add(p);

                p.kill();
                //TODO Show message somehow
                //setActive(true, p.getDisplayName() + " disconnected..");

                if (entireTeamDisconnected(p.getTeam())) {
                    active = false; //Don't check winstate
                    TimeUtils.executeInSync(100, new Runnable() {
                        @Override
                        public void run() {
                            if (p.getTeam().getTeamNumber() == team1.getTeamNumber())
                                forfeit(team2);
                            else
                                forfeit(team1);
                        }
                    }, p.getWorld());
                }
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

    public void addPlayer(Player player) throws IOException {
        MatchFoundPacket packet = new MatchFoundPacket(player.getClient());
        packet.writePacket(player.getX(), player.getY());

        spawnAllEntitiesFor(player);

        MapSettingsPacket packet3 = new MapSettingsPacket(player.getClient());
        packet3.writePacket(world.getWorldMap());

        MatchStatusPacket packet2 = new MatchStatusPacket(player.getClient());
        packet2.writePacket(active, lastActiveReason);

        world.requestEntityUpdate();
    }
}
