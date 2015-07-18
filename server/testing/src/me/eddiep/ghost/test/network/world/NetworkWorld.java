package me.eddiep.ghost.test.network.world;

import me.eddiep.ghost.game.match.entities.Entity;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.world.WorldImpl;
import me.eddiep.ghost.game.match.world.timeline.*;
import me.eddiep.ghost.network.Client;
import me.eddiep.ghost.test.game.NetworkMatch;
import me.eddiep.ghost.test.game.User;
import me.eddiep.ghost.test.network.TcpUdpClient;
import me.eddiep.ghost.test.network.packet.BulkEntityStatePacket;
import me.eddiep.ghost.test.network.packet.DespawnEntityPacket;
import me.eddiep.ghost.test.network.packet.PlayerStatePacket;
import me.eddiep.ghost.test.network.packet.SpawnEntityPacket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NetworkWorld extends WorldImpl {
    private NetworkMatch match;
    private ArrayList<User> connectedPlayers = new ArrayList<User>();
    private ArrayList<User> connectedSpectators = new ArrayList<>();
    private ArrayList<Short> ids = new ArrayList<>();
    private TimelineCursor presentCursor;
    private TimelineCursor spectatorCursor;

    public NetworkWorld(NetworkMatch match) {
        super(match);
        this.match = match;
    }

    public NetworkMatch getNetworkMatch() {
        return match;
    }

    @Override
    public void onLoad() {
        presentCursor = timeline.createCursor();
        spectatorCursor = timeline.createCursor();
        spectatorCursor.setDistanceFromPresent(3000);

        presentCursor.setListener(TIMELINE_CURSOR_LISTENER);
        spectatorCursor.setListener(TIMELINE_CURSOR_LISTENER);
    }

    @Override
    protected void onTimelineTick() {
        presentCursor.tick();
        spectatorCursor.tick();
    }

    @Override
    public void spawnEntity(Entity entity) {
        if (entity.getID() == -1)
            setID(entity);

        super.spawnEntity(entity);
    }

    @Override
    public void requestEntityUpdate() {
        for (User p : connectedPlayers) {
            try {
                presentCursor.sendClientSnapshot(p.getClient());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (User s : connectedSpectators) {
            try {
                spectatorCursor.sendClientSnapshot(s.getClient());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addPlayer(User user) throws IOException {
        connectedPlayers.add(user);

        if (presentCursor.position() > -1) {
            for (EntitySnapshot snapshot : presentCursor.get().getEntitySnapshots()) {
                spawnEntityFor(user, snapshot.toSpawnSnapshot());
            }
        }
    }

    public void addSpectator(User user) throws IOException {
        connectedSpectators.add(user);

        if (spectatorCursor.position() > -1) {
            for (EntitySnapshot snapshot : spectatorCursor.get().getEntitySnapshots()) {
                spawnEntityFor(user, snapshot.toSpawnSnapshot());
            }
        }
    }

    @Override
    public void updateClient(Client client) throws IOException {
        WorldSnapshot snapshot = presentCursor.get();

        updateClient(client, snapshot);
    }

    @Override
    public void updateClient(Client client, WorldSnapshot snapshot) throws IOException {
        if (!(client instanceof TcpUdpClient))
            return;

        TcpUdpClient c = (TcpUdpClient)client;

        BulkEntityStatePacket packet = new BulkEntityStatePacket(c);
        packet.writePacket(snapshot, match);
    }

    public void despawnEntityFor(User n, EntityDespawnSnapshot e) throws IOException {
        DespawnEntityPacket packet = new DespawnEntityPacket(n.getClient());
        packet.writePacket(e);
    }

    public void spawnEntityFor(User n, EntitySpawnSnapshot e) throws IOException {
        SpawnEntityPacket packet = new SpawnEntityPacket(n.getClient());
        byte type;
        if (n instanceof PlayableEntity) {
            PlayableEntity np = (PlayableEntity)n;
            if (e.isPlayableEntity()) {
                if (np.getTeam().isAlly(e.getID())) {
                    type = 0;
                } else {
                    type = 1;
                }
            } else if (e.isTypeableEntity()) {
                type = e.getType();
            } else {
                return;
            }
        } else {
            if (e.isPlayableEntity()) {
                if (getMatch().getTeam1().isAlly(e.getID()))
                    type = 0;
                else
                    type = 1;
            } else if (e.isTypeableEntity()) {
                type = e.getType();
            } else {
                return;
            }
        }

        packet.writePacket(e, type);
    }

    private void setID(Entity entity) {
        short id = 0;
        do {
            id++;
        } while (ids.contains(id));

        entity.setID(id);
        ids.add(entity.getID());
    }

    private void spawnForSpectators(EntitySpawnSnapshot entity) {
        for (User user : connectedSpectators) {
            if (!user.isConnected())
                continue;

            try {
                spawnEntityFor(user, entity);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void spawnForPlayers(EntitySpawnSnapshot entity) {
        for (User user : connectedPlayers) {
            if (!user.isConnected())
                continue;

            try {
                spawnEntityFor(user, entity);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void despawnForSpectators(EntityDespawnSnapshot entity) {
        for (User user : connectedSpectators) {
            if (!user.isConnected())
                continue;

            try {
                despawnEntityFor(user, entity);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void despawnForPlayers(EntityDespawnSnapshot entity) {
        for (User user : connectedPlayers) {
            if (!user.isConnected())
                continue;

            try {
                despawnEntityFor(user, entity);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updatePlayableForPlayers(PlayableSnapshot snapshot) {
        for (User user : connectedPlayers) {
            if (!user.isConnected())
                continue;

            PlayerStatePacket packet = new PlayerStatePacket(user.getClient());
            try {
                packet.writePacket(snapshot);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updatePlayableForSpectators(PlayableSnapshot snapshot) {
        for (User user : connectedSpectators) {
            if (!user.isConnected())
                continue;

            PlayerStatePacket packet = new PlayerStatePacket(user.getClient());
            try {
                packet.writePacket(snapshot);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private final TimelineCursorListener TIMELINE_CURSOR_LISTENER = new TimelineCursorListener() {
        @Override
        public void onTick(TimelineCursor cursor) {
            boolean isSpectator = cursor == spectatorCursor;

            WorldSnapshot snapshot = cursor.get();

            if (snapshot.getEntitySpawnSnapshots() != null && snapshot.getEntitySpawnSnapshots().length > 0) {
                for (EntitySpawnSnapshot spawnSnapshot : snapshot.getEntitySpawnSnapshots()) {
                    if (isSpectator) {
                        spawnForSpectators(spawnSnapshot);
                    } else {
                        spawnForPlayers(spawnSnapshot);
                    }
                }
            }

            if (snapshot.getEntityDespawnSnapshots() != null && snapshot.getEntityDespawnSnapshots().length > 0) {
                for (EntityDespawnSnapshot despawnSnapshot : snapshot.getEntityDespawnSnapshots()) {
                    if (isSpectator) {
                        despawnForSpectators(despawnSnapshot);
                    } else {
                        despawnForPlayers(despawnSnapshot);
                    }
                }
            }

            if (snapshot.getPlayableChanges() != null && snapshot.getPlayableChanges().length > 0) {
                for (PlayableSnapshot playableSnapshot : snapshot.getPlayableChanges()) {
                    if (isSpectator) {
                        updatePlayableForSpectators(playableSnapshot);
                    } else {
                        updatePlayableForPlayers(playableSnapshot);
                    }
                }
            }
        }
    };

    public List<User> getPlayers() {
        return connectedPlayers;
    }
}
