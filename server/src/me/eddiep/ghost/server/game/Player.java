package me.eddiep.ghost.server.game;

import me.eddiep.ghost.server.game.queue.PlayerQueue;
import me.eddiep.ghost.server.game.util.Vector2f;
import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.network.PlayerFactory;
import me.eddiep.ghost.server.network.packet.impl.ClientStatePacket;
import me.eddiep.ghost.server.utils.events.EventEmitter;

import java.io.IOException;
import java.util.UUID;

public class Player extends EventEmitter {
    private static final float SPEED = 7f;
    private String username;
    private UUID session;
    private Client client;
    private boolean isInQueue;
    private boolean isInMatch;
    private boolean isDead;
    private boolean isReady;
    private PlayerQueue queue;
    private Match match;
    private Vector2f position;
    private Vector2f velocity;
    private int lastRecordedTick;
    private boolean frozen;
    private boolean visible;

    static Player createPlayer(String username) {
        Player player = new Player();
        player.username = username;
        do {
            player.session = UUID.randomUUID();
        } while (PlayerFactory.findPlayerByUUID(player.session) != null);

        return player;
    }

    private Player() {
        register("movementRequested");
        register("queueJoined");
        register("matchJoined");
    }

    public String getUsername() {
        return username;
    }

    public UUID getSession() {
        return session;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client c) {
        if (this.client != null)
            throw new IllegalStateException("This Player already has a client!");

        this.client = c;
    }

    public boolean isInQueue() {
        return isInQueue;
    }

    public boolean isInMatch() {
        return getMatch() != null;
    }

    public boolean isDead() {
        return isDead;
    }

    public void kill() {
        isDead = true;
        //TODO Update state..
    }

    public void freeze() {
        frozen = true;
        //TODO Update state..
    }

    public void unfreeze() {
        frozen = false;
        //TODO Update state..
    }

    public boolean isFrozen() {
        return frozen;
    }

    public PlayerQueue getQueue() {
        return queue;
    }

    public void setQueue(PlayerQueue queue) {
        this.queue = queue;
    }

    public void setInQueue(boolean inQueue) {
        this.isInQueue = inQueue;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public Match getMatch() {
        return match;
    }

    public Vector2f getPosition() {
        return position;
    }

    public Vector2f getVelocity() {
        return velocity;
    }

    public void setPosition(Vector2f position) {
        this.position = position;
    }

    public void setPosition(float x, float y) {
        setPosition(new Vector2f(x, y));
    }

    public void setVelocity(Vector2f velocity) {
        this.velocity = velocity;
    }

    public void setVelocity(float xvel, float yvel) {
        setVelocity(new Vector2f(xvel, yvel));
    }

    public void updateState() throws IOException {
        if (!isInMatch)
            return;

        if (getOpponent().visible)
            updateStateFor(getOpponent());

        updateStateFor(this);
    }

    public void updateStateFor(Player player) throws IOException {
        if (player == null)
            return;
        ClientStatePacket packet = new ClientStatePacket(player.getClient());
        packet.writePacket(player);
    }

    public void moveTowards(float targetX, float targetY) {
        float x = position.x;
        float y = position.y;

        float asdx = targetX - x;
        float asdy = targetY - y;
        float inv = (float) Math.atan2(asdy, asdx);


        velocity.x = (float) (Math.cos(inv)*SPEED);
        velocity.y = (float) (Math.sin(inv)*SPEED);
        try {
            updateStateFor(this);

            if (visible)
                getOpponent().updateStateFor(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Player getOpponent() {
        if (!isInMatch)
            return null;

        if (getMatch().getPlayer1().equals(this))
            return getMatch().getPlayer2();
        else if (getMatch().getPlayer2().equals(this))
            return getMatch().getPlayer1();
        else
            return null;
    }

    public void setLastRecordedTick(int lastRecordedTick) {
        this.lastRecordedTick = lastRecordedTick;
    }

    public int getLastRecordedTick() {
        return lastRecordedTick;
    }

    private long lastUpdate = 0;
    public void tick() {
        position.x += velocity.x;
        position.y += velocity.y;

        if (getMatch().getTimeElapsed() - lastUpdate >= 50) {
            lastUpdate = getMatch().getTimeElapsed();
            try {
                updateState();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Player) {
            Player p = (Player)obj;
            if (p.getSession().equals(getSession()))
                return true;
        }
        return false;
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }

    public boolean isVisible() {
        return visible;
    }
}
