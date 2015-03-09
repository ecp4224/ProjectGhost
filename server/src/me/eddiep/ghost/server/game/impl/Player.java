package me.eddiep.ghost.server.game.impl;

import me.eddiep.ghost.server.game.Entity;
import me.eddiep.ghost.server.game.Team;
import me.eddiep.ghost.server.game.TypeableEntity;
import me.eddiep.ghost.server.game.queue.PlayerQueue;
import me.eddiep.ghost.server.game.util.Vector2f;
import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.network.packet.impl.SpawnEntityPacket;

import java.io.IOException;
import java.util.UUID;

public class Player extends Entity {
    public static final int WIDTH = 64;
    public static final int HEIGHT = 64;
    private static final float SPEED = 7f;

    private String username;
    private UUID session;
    private Client client;
    private boolean isInQueue;
    private boolean isDead;
    private boolean isReady;
    private boolean oldVisibleState;
    private PlayerQueue queue;
    private int lastRecordedTick;
    private boolean frozen;
    private Vector2f target;

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

    public Team getTeam() {
        return containingMatch == null ? null : containingMatch.getTeamFor(this);
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

    public void updateState() throws IOException {
        if (!isInMatch())
            return;

        if ((!visible && oldVisibleState) || visible) {
            for (Player opp : getOpponents()) {
                this.updateStateFor(opp); //Update this state for the opponent
            }

            oldVisibleState = visible;
        }

        for (Player ally : getTeam().getTeamMembers()) { //This loop will include all allies and this player
            ally.updateStateFor(this);
        }

        //this.updateStateFor(this); //Update this state for the this player
    }

    public void spawnEntity(Entity entity) throws IOException {
        if (entity.getID() != getID()) {
            SpawnEntityPacket packet = new SpawnEntityPacket(client);
            byte type;
            if (entity instanceof Player) {
                Player p = (Player)entity;
                if (getTeam().isAlly(p)) {
                    type = 0;
                } else {
                    type = 1;
                }
            } else if (entity instanceof TypeableEntity) {
                type = ((TypeableEntity)entity).getType();
            } else {
                return;
            }

            packet.writePacket(entity, type);
        }
    }

    @Override
    public String getName() {
        return getUsername();
    }

    @Override
    public void setName(String name) { }

    public void moveTowards(float targetX, float targetY) {
        float x = position.x;
        float y = position.y;

        float asdx = targetX - x;
        float asdy = targetY - y;
        float inv = (float) Math.atan2(asdy, asdx);


        velocity.x = (float) (Math.cos(inv)*SPEED);
        velocity.y = (float) (Math.sin(inv)*SPEED);

        target = new Vector2f(targetX, targetY);

        try {
            updateState();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fireTowards(float targetX, float targetY) {
        float x = position.x;
        float y = position.y;

        float asdx = targetX - x;
        float asdy = targetY - y;
        float inv = (float) Math.atan2(asdy, asdx);

        Vector2f velocity = new Vector2f((float)Math.cos(inv)*SPEED, (float)Math.sin(inv)*SPEED);


    }

    public Player[] getOpponents() {
        if (!isInMatch())
            return new Player[0];

        if (getMatch().getTeam1().isAlly(this))
            return getMatch().getTeam2().getTeamMembers();
        else if (getMatch().getTeam2().isAlly(this))
            return getMatch().getTeam1().getTeamMembers();
        else
            return new Player[0];
    }

    public boolean hasTarget() {
        return target != null;
    }

    public Vector2f getTarget() {
        return target;
    }

    public void setLastRecordedTick(int lastRecordedTick) {
        this.lastRecordedTick = lastRecordedTick;
    }

    public int getLastRecordedTick() {
        return lastRecordedTick;
    }

    private long lastUpdate = 0;
    public void resetUpdateTimer() {
        lastUpdate = 0;
    }

    @Override
    public void tick() {
        if (hasTarget()) {
            if (Math.abs(position.x - target.x) < 8 && Math.abs(position.y - target.y) < 8) {
                setPosition(target);
                target = null;
                setVelocity(new Vector2f(0f, 0f));
                try {
                    updateState();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

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
}
