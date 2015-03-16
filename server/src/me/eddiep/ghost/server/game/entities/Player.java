package me.eddiep.ghost.server.game.entities;

import me.eddiep.ghost.server.game.Entity;
import me.eddiep.ghost.server.game.queue.PlayerQueue;
import me.eddiep.ghost.server.game.util.Vector2f;
import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.network.packet.impl.DespawnEntityPacket;
import me.eddiep.ghost.server.network.packet.impl.PlayerStatePacket;
import me.eddiep.ghost.server.network.packet.impl.SpawnEntityPacket;

import java.io.IOException;
import java.util.UUID;

public class Player extends Entity {
    public static final int WIDTH = 64;
    public static final int HEIGHT = 64;
    private static final float SPEED = 7f;
    private static final float BULLET_SPEED = 12f;
    private static final float VISIBLE_TIMER = 800f;
    private static final byte MAX_LIVES = 3;


    private String username;
    private UUID session;
    private Client client;
    private boolean isDead;
    private boolean isReady;
    private PlayerQueue queue;
    private int lastRecordedTick;
    private boolean frozen;
    private Vector2f target;
    private byte lives = MAX_LIVES;
    boolean wasHit;
    long lastHit;

    private long lastActive;
    private long logonTime;

    static Player createPlayer(String username) {
        Player player = new Player();
        player.username = username;
        do {
            player.session = UUID.randomUUID();
        } while (PlayerFactory.findPlayerByUUID(player.session) != null);
        player.logonTime = player.lastActive = System.currentTimeMillis();

        return player;
    }

    private Player() {
        initDatabaseInformation();
    }

    private void initDatabaseInformation() {
        //TODO Load and/or create any database information here
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

    public boolean isUDPConnected() {
        return client != null && client.getPort() != -1;
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
        return queue != null;
    }

    public boolean isInMatch() {
        return getMatch() != null;
    }

    public boolean isDead() {
        return isDead;
    }

    public byte getLives() {
        return lives;
    }

    public void subtractLife() {
        lives--;
        if (lives <= 0) {
            isDead = true;
            frozen = true;
            setVisible(true);
        }
        try {
            updatePlayerState();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addLife() {
        lives++;
        if (isDead) {
            isDead = false;
            frozen = false;
            setVisible(false);
        }
        try {
            updatePlayerState();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resetLives() {
        lives = MAX_LIVES;
        if (isDead) {
            isDead = false;
            frozen = false;
            setVisible(false);
        }
        try {
            updatePlayerState();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void kill() {
        lives = 0;
        isDead = true;
        frozen = true;
        setVisible(true);
        try {
            updatePlayerState();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void freeze() {
        frozen = true;
        try {
            updatePlayerState();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void unfreeze() {
        frozen = false;
        try {
            updatePlayerState();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isFrozen() {
        return frozen;
    }

    public PlayerQueue getQueue() {
        return queue;
    }

    public void setQueue(PlayerQueue queue) {
        this.queue = queue;
        lastActive = System.currentTimeMillis();
    }

    public long getLastActiveTime() {
        return lastActive;
    }

    public long getLogonTime() {
        return logonTime;
    }

    public long getLoginDuration() {
        return System.currentTimeMillis() - logonTime;
    }

    public long getLastActionDuration() {
        return System.currentTimeMillis() - lastActive;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

    public void updatePlayerState() throws IOException {
        if (!isInMatch() || !isUDPConnected())
            return;

        for (Player oop : getOpponents()) {
            this.updatePlayerStateFor(oop);
        }

        for (Player ally : getAllies()) {
            this.updatePlayerStateFor(ally);
        }
    }

    public void updatePlayerStateFor(Player p) throws IOException {
        PlayerStatePacket packet = new PlayerStatePacket(p.getClient());
        packet.writePacket(this);
    }

    @Override
    public void updateState() throws IOException {
        if (!isInMatch() || !isUDPConnected())
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
        if (!isUDPConnected())
            throw new IllegalStateException("This client is not connected!");

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

    public void despawnEntity(Entity e) throws IOException {
        if (!isUDPConnected())
            throw new IllegalStateException("This client is not connected!");

        DespawnEntityPacket packet = new DespawnEntityPacket(client);
        packet.writePacket(e);
    }

    @Override
    public String getName() {
        return getUsername();
    }

    @Override
    public void setName(String name) { }

    public void moveTowards(float targetX, float targetY) {
        if (!isUDPConnected())
            return;

        lastActive = System.currentTimeMillis();

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

    private long lastFire;
    private boolean didFire = false;
    public void fireTowards(float targetX, float targetY) {
        if (!isUDPConnected() || System.currentTimeMillis() - lastFire < 300)
            return;

        lastActive = System.currentTimeMillis();

        lastFire = System.currentTimeMillis();
        didFire = true;
        if (!visible) {
            setVisible(true);
        }

        float x = position.x;
        float y = position.y;

        float asdx = targetX - x;
        float asdy = targetY - y;
        float inv = (float) Math.atan2(asdy, asdx);

        Vector2f velocity = new Vector2f((float)Math.cos(inv)*BULLET_SPEED, (float)Math.sin(inv)*BULLET_SPEED);

        Bullet b = new Bullet(this);
        b.setPosition(getPosition().cloneVector());
        b.setVelocity(velocity);

        try {
            getMatch().spawnEntity(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public Player[] getAllies() {
        if (getTeam() == null)
            return new Player[0];

        return getTeam().getTeamMembers();
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

        if (didFire) {
            if (visible && System.currentTimeMillis() - lastFire >= VISIBLE_TIMER) {
                setVisible(false);
                didFire = false;
            }
        } else if (wasHit) {
            if (visible && System.currentTimeMillis() - lastHit >= VISIBLE_TIMER) {
                setVisible(false);
                wasHit = false;
            }
        }

        super.tick();
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

    public void disconnected() {
        client = null;

        lastActive = System.currentTimeMillis();
    }
}
