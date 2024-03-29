package com.boxtrotstudio.ghost.common.network.world;

import com.boxtrotstudio.ghost.common.game.NetworkMatch;
import com.boxtrotstudio.ghost.common.game.User;
import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.common.network.packet.*;
import com.boxtrotstudio.ghost.game.match.entities.Entity;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.entities.map.Text;
import com.boxtrotstudio.ghost.game.match.world.ParticleEffect;
import com.boxtrotstudio.ghost.game.match.world.WorldImpl;
import com.boxtrotstudio.ghost.game.match.world.timeline.*;
import com.boxtrotstudio.ghost.network.Client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NetworkWorld extends WorldImpl {
    private NetworkMatch match;
    private ArrayList<User> connectedPlayers = new ArrayList<>();
    private ArrayList<User> connectedSpectators = new ArrayList<>();
    private TimelineCursor presentCursor;
    private TimelineCursor spectatorCursor;
    private String map;

    public NetworkWorld(String mapName, NetworkMatch match) {
        super(match);
        this.match = match;
        this.map = mapName;
    }

    public NetworkMatch getNetworkMatch() {
        return match;
    }

    @Override
    public void onLoad() {
        super.onLoad();

        presentCursor = timeline.createCursor();
        spectatorCursor = timeline.createCursor();
        spectatorCursor.setDistanceFromPresent(3000);

        presentCursor.setListener(TIMELINE_CURSOR_LISTENER);
        spectatorCursor.setListener(TIMELINE_CURSOR_LISTENER);
    }

    @Override
    public void displayText(Text text) {
        super.displayText(text);

        for (User p : connectedPlayers) {
            if (!p.isConnected())
                continue;

            DisplayTextPacket packet = new DisplayTextPacket(p.getClient());
            try {
                packet.writePacket(text);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void removeText(Text text) {
        super.removeText(text);

        for (User p : connectedPlayers) {
            if (!p.isConnected())
                continue;

            RemoveTextPacket packet = new RemoveTextPacket(p.getClient());
            try {
                packet.writePacket(text);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String mapName() {
        return map;
    }

    @Override
    protected void onTimelineTick() {
        presentCursor.tick();
        spectatorCursor.tick();
    }

    @Override
    public void requestEntityUpdate() {
        for (User p : connectedPlayers) {
            if (!p.isConnected())
                continue;
            try {
                updateClient(p.getClient(), presentCursor.get());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (User s : connectedSpectators) {
            if (!s.isConnected())
                continue;
            try {
                updateClient(s.getClient(), spectatorCursor.get());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addPlayer(User user) throws IOException {
        connectedPlayers.add(user);

        if (!user.isConnected())
            return;

        if (presentCursor.position() > -1 && timeline.size() > 0) {
            for (EntitySnapshot snapshot : presentCursor.get().getEntitySnapshots()) {
                if (snapshot == null)
                    continue;
                spawnEntityFor(user, snapshot.toSpawnSnapshot());
            }
        }
    }

    public void addSpectator(User user) {
        connectedSpectators.add(user);

        /*if (spectatorCursor.position() > -1) {
            for (EntitySnapshot snapshot : spectatorCursor.get().getEntitySnapshots()) {
                if (snapshot == null)
                    continue;
                spawnEntityFor(user, snapshot.toSpawnSnapshot());
            }
        }*/
    }

    public void updateClient(Client client, WorldSnapshot snapshot) throws IOException {
        if (!(client instanceof BasePlayerClient))
            return;

        BasePlayerClient c = (BasePlayerClient)client;

        BulkEntityStatePacket packet = new BulkEntityStatePacket(c);
        packet.writePacket(snapshot, match);
    }

    @Override
    public void spawnEntityFor(PlayableEntity player, Entity entity) {
        if (player instanceof User) {
            try {
                spawnEntityFor((User) player, EntitySpawnSnapshot.createEvent(entity));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void despawnEntityFor(PlayableEntity player, Entity entity) {
        if (player instanceof User) {
            try {
                despawnEntityFor((User) player, EntityDespawnSnapshot.createEvent(entity));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void despawnEntityFor(User n, EntityDespawnSnapshot e) throws IOException {
        DespawnEntityPacket packet = new DespawnEntityPacket(n.getClient());
        packet.writePacket(e);
    }

    public void spawnEntityFor(User n, EntitySpawnSnapshot e) throws IOException {
        SpawnEntityPacket packet = new SpawnEntityPacket(n.getClient());
        short type;
        if (!connectedSpectators.contains(n)) {
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

    private void spawnParticleForPlayers(ParticleEffect effect, int duration, int size, float x, float y, double rotation) {
        for (User u : connectedPlayers) {
            if (!u.isConnected())
                continue;

            SpawnParticleEffectPacket packet = new SpawnParticleEffectPacket(u.getClient());
            try {
                packet.writePacket(effect, duration, size, x, y, rotation);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void spawnParticleForSpectators(ParticleEffect effect, int duration, int size, float x, float y, double rotation) {
        for (User u : connectedSpectators) {
            if (!u.isConnected())
                continue;

            SpawnParticleEffectPacket packet = new SpawnParticleEffectPacket(u.getClient());
            try {
                packet.writePacket(effect, duration, size, x, y, rotation);
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
                    if (spawnSnapshot.isParticle()) {
                        //DEPRECATED
                        /*String[] data = spawnSnapshot.getName().split(":");
                        int duration = Integer.parseInt(data[0]);
                        int size = Integer.parseInt(data[1]);
                        double rotation = Double.parseDouble(data[2]);

                        if (isSpectator) {
                            spawnParticleForSpectators(ParticleEffect.fromByte((byte) spawnSnapshot.getType()), duration, size, spawnSnapshot.getX(), spawnSnapshot.getY(), rotation);
                        } else {
                            spawnParticleForPlayers(ParticleEffect.fromByte((byte) spawnSnapshot.getType()), duration, size, spawnSnapshot.getX(), spawnSnapshot.getY(), rotation);
                        }*/
                    } else {
                        if (isSpectator) {
                            spawnForSpectators(spawnSnapshot);
                        } else {
                            spawnForPlayers(spawnSnapshot);
                        }
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

            if (snapshot.getEvents() != null && snapshot.getEvents().length > 0) {
                for (EventSnapshot eventSnapshot : snapshot.getEvents()) {
                    if (isSpectator) {
                        sendEventForSpectators(eventSnapshot);
                    } else {
                        sendEventForPlayers(eventSnapshot);
                    }
                }
            }
        }
    };

    private void sendEventForPlayers(EventSnapshot eventSnapshot) {
        EventPacket packet = null;
        for (User user : connectedPlayers) {
            if (!user.isConnected())
                return;

            if (packet == null) {
                packet = new EventPacket(user.getClient());
            } else {
                packet.reuseFor(user.getClient());
            }

            try {
                packet.writeAndPreservePacket(eventSnapshot);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendEventForSpectators(EventSnapshot eventSnapshot) {
        EventPacket packet = null;
        for (User user : connectedSpectators) {
            if (!user.isConnected())
                return;

            if (packet == null) {
                packet = new EventPacket(user.getClient());
            } else {
                packet.reuseFor(user.getClient());
            }

            try {
                packet.writeAndPreservePacket(eventSnapshot);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<User> getPlayers() {
        return connectedPlayers;
    }

    public void removeSpectator(User player) {
        this.connectedSpectators.remove(player);
    }

    public List<User> getSpectators() {
        return Collections.unmodifiableList(this.connectedSpectators);
    }
}
