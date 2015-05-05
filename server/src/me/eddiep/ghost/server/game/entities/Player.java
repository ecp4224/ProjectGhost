package me.eddiep.ghost.server.game.entities;

import me.eddiep.ghost.server.Main;
import me.eddiep.ghost.server.game.ActiveMatch;
import me.eddiep.ghost.server.game.Entity;
import me.eddiep.ghost.server.game.queue.PlayerQueue;
import me.eddiep.ghost.server.game.queue.Queues;
import me.eddiep.ghost.server.game.ranking.Rank;
import me.eddiep.ghost.server.game.stats.TrackingMatchStats;
import me.eddiep.ghost.server.game.util.*;
import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.network.packet.impl.*;
import me.eddiep.ghost.server.network.sql.PlayerData;
import me.eddiep.ghost.server.network.sql.PlayerUpdate;
import me.eddiep.ghost.server.utils.PRunnable;

import java.io.IOException;
import java.util.*;

public class Player extends Entity {
    public static final int WIDTH = 48;
    public static final int HEIGHT = 48;
    private static final float SPEED = 6f;
    private static final float BULLET_SPEED = 12f;
    private static final float VISIBLE_TIMER = 800f;
    private static final byte MAX_LIVES = 3;


    private TrackingMatchStats trackingMatchStats;
    private long visibleTime;
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
    int hatTrickCount;

    private long lastActive;
    private long logonTime;

    private HashMap<Integer, Request> requests = new HashMap<>();

    //===SQL DATA===
    HashMap<Byte, Integer> winHash = new HashMap<>();
    HashMap<Byte, Integer> loseHash = new HashMap<>();
    long shotsHit;
    long shotsMissed;
    int hatTricks;
    private long pid;
    private String displayName;
    Set<Long> playersKilled;
    private Rank ranking;
    private Set<Long> friends;
    //===SQL DATA===


    /**
     * Create a new user with the provided username and SQL Data
     * @param username The username of this player
     * @param sqlData The SQL data for associated with this player
     * @return A new {@link Player} object
     */
    static Player createPlayer(String username, PlayerData sqlData) {
        Player player = new Player();
        player.username = username;
        do {
            player.session = UUID.randomUUID();
        } while (PlayerFactory.findPlayerByUUID(player.session) != null);
        player.logonTime = player.lastActive = System.currentTimeMillis();
        player.loadSQLData(sqlData);
        return player;
    }

    private Player() {
    }

    private void loadSQLData(PlayerData sqlData) {
        pid = sqlData.getId();
        winHash = sqlData.getWins();
        loseHash = sqlData.getLoses();
        shotsHit = sqlData.getShotsHit();
        shotsMissed = sqlData.getShotsMissed();
        displayName = sqlData.getDisplayname();
        playersKilled = sqlData.getPlayersKilled();
        hatTricks = sqlData.getHatTrickCount();
        ranking = sqlData.getRank();
        friends = sqlData.getFriends();
    }


    void saveSQLData(Queues type, boolean won, int value) {
        PlayerUpdate update = new PlayerUpdate(this);

        if (won)
            update.updateWinsFor(type, value);
        else
            update.updateLosesFor(type, value);
        update.updateShotsMade(shotsHit);
        update.updateShotsMissed(shotsMissed);
        update.updatePlayersKilled(playersKilled);
        update.updateHatTricks(hatTricks);
        update.updateRank(ranking);

        update.push();
    }

    /**
     * Get the total amount of bullets fired by this player
     * @return The total amount of bullets fired
     */
    public long getTotalShotsFired() {
        return shotsHit + shotsMissed;
    }

    /**
     * Calculate how accurate this player's shots are overall
     * @return The accuracy of this player overall
     */
    public double getAccuracy() {
        return (double) shotsHit / (double)getTotalShotsFired();
    }

    /**
     * Get the amount of games won per queue. The hashmap is <byte, int>, where the byte represents the queue and the int
     * is the amount of games won in that queue
     * @return
     */
    public HashMap<Byte, Integer> getWinHash() {
        return winHash;
    }

    /**
     * Get the amount of games lost per queue. The hashmap is <byte, int>, where the byte represents the queue and the int
     * is the amount of games lost in that queue
     * @return
     */
    public HashMap<Byte, Integer> getLoseHash() {
        return loseHash;
    }

    /**
     * Get the player id of this player. This is unique per player
     * @return The unique player id for this player
     */
    public long getPlayerID() {
        return pid;
    }

    /**
     * Get the username for this player
     * @return The username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Get this player's current session.
     * @return The current session for this player
     */
    public UUID getSession() {
        return session;
    }

    /**
     * Get the currently connected client for this player.
     * @return The currently connected {@link me.eddiep.ghost.server.network.Client}
     */
    public Client getClient() {
        return client;
    }

    /**
     * Check if this player is connected via UDP.
     * @return True if this player is, otherwise false
     */
    public boolean isUDPConnected() {
        return client != null && client.getPort() != -1;
    }

    /**
     * Set the client for this player. <b>This method should only be invoked by the {@link me.eddiep.ghost.server.network.Client} class!</b>
     * @param c The new client
     */
    public void setClient(Client c) {
        if (this.client != null)
            throw new IllegalStateException("This Player already has a client!");


        this.client = c;
    }

    /**
     * Get the team this player is on. If this player is not in a match, then null is returned.
     * @return The team for this player
     */
    public Team getTeam() {
        return containingMatch == null ? null : containingMatch.getTeamFor(this);
    }

    /**
     * Check whether or not this player is currently waiting in queue
     * @return True if the player is waiting in queue, otherwise false
     */
    public boolean isInQueue() {
        return queue != null;
    }

    /**
     * Check whether or not this player is currently in a match
     * @return True if the player is in a match, otherwise false
     */
    public boolean isInMatch() {
        return getMatch() != null;
    }

    /**
     * Check whether this player is dead
     * @return True if the player is dead, otherwise false
     */
    public boolean isDead() {
        return isDead;
    }

    /**
     * Get the amount of lives this player has
     * @return The amount of lives as a byte
     */
    public byte getLives() {
        return lives;
    }

    /**
     * Subtract 1 life from this player and update all other players
     *
     * <b>The player must be in a match, otherwise a {@link java.lang.IllegalStateException} exception will be thrown</b>
     */
    public void subtractLife() {
        if (!isInMatch())
            throw new IllegalStateException("This player is not in a match!");

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

    /**
     * Add 1 life to this player and update all other players
     *
     * <b>The player must be in a match, otherwise a {@link java.lang.IllegalStateException} exception will be thrown</b>
     */
    public void addLife() {
        if (!isInMatch())
            throw new IllegalStateException("This player is not in a match!");

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

    /**
     * Reset this player's lives and update all other players
     *
     * <b>The player must be in a match, otherwise a {@link java.lang.IllegalStateException} exception will be thrown</b>
     */
    public void resetLives() {
        if (!isInMatch())
            throw new IllegalStateException("This player is not in a match!");

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

    /**
     * Kill this player and update all other players
     *
     * <b>The player must be in a match, otherwise a {@link java.lang.IllegalStateException} exception will be thrown</b>
     */
    public void kill() {
        if (!isInMatch())
            throw new IllegalStateException("This player is not in a match!");

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

    /**
     * Freeze this player and update all other players
     *
     * <b>The player must be in a match, otherwise a {@link java.lang.IllegalStateException} exception will be thrown</b>
     */
    public void freeze() {
        if (!isInMatch())
            throw new IllegalStateException("This player is not in a match!");

        frozen = true;
        try {
            updatePlayerState();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Unfreeze this player and update all other players
     *
     * <b>The player must be in a match, otherwise a {@link java.lang.IllegalStateException} exception will be thrown</b>
     */
    public void unfreeze() {
        if (!isInMatch())
            throw new IllegalStateException("This player is not in a match!");

        frozen = false;
        try {
            updatePlayerState();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check whether this player is frozen or not.
     * @return True if the player is frozen, otherwise false
     */
    public boolean isFrozen() {
        return frozen;
    }

    /**
     * Get the current queue this player is waiting in
     * @return The current queue this player is waiting in
     */
    public PlayerQueue getQueue() {
        return queue;
    }

    /**
     * Set the queue this player is currently waiting in <b>This method should only be called by {@link me.eddiep.ghost.server.game.queue.PlayerQueue} objects!</b>
     * @param queue The queue the player joined
     */
    public void setQueue(PlayerQueue queue) {
        this.queue = queue;
        lastActive = System.currentTimeMillis();
    }

    /**
     * Get the {@link me.eddiep.ghost.server.game.stats.TrackingMatchStats} object for this player
     * @return The {@link me.eddiep.ghost.server.game.stats.TrackingMatchStats} object for this player
     */
    public TrackingMatchStats getTrackingStats() {
        return trackingMatchStats;
    }

    /**
     * Get the last time this player was active as a unix timestamp.
     * @return The last time this player was active
     * @see System#currentTimeMillis()
     */
    public long getLastActiveTime() {
        return lastActive;
    }

    /**
     * Get the time this player logged in as a unix timestamp
     * @return The time this player logged in
     * @see System#currentTimeMillis()
     */
    public long getLogonTime() {
        return logonTime;
    }

    /**
     * Get how long this player has been logged into the current session
     * @return The duration this session has been active
     * @see System#currentTimeMillis()
     */
    public long getLoginDuration() {
        return System.currentTimeMillis() - logonTime;
    }

    /**
     * Get how long it has been since the player has done something
     * @return The duration since the player last did something
     * @see System#currentTimeMillis()
     */
    public long getLastActionDuration() {
        return System.currentTimeMillis() - lastActive;
    }

    /**
     * Check to see if this player is ready
     * @return True if the player is ready, otherwise false
     */
    public boolean isReady() {
        return isReady;
    }

    /**
     * Set this player's ready state <b>THIS DOES NOT UPDATE THE CLIENT. THIS METHOD SHOULD ONLY BE CALLED FROM {@link me.eddiep.ghost.server.network.packet.impl.ReadyPacket}</b>
     * @param isReady Whether this player is ready
     */
    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

    @Override
    public void setMatch(ActiveMatch containingMatch) {
        super.setMatch(containingMatch);
        lastActive = System.currentTimeMillis();

        if (containingMatch != null)
            trackingMatchStats = new TrackingMatchStats(this);
    }

    /**
     * Get the displayname of this player
     * @return The displayname
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Set the displayname of this player and save it. <b>THIS DOES NOT UPDATE THE CLIENT</b>
     * @param displayName The new displayname
     */
    public void setDisplayName(String displayName) {
        PlayerUpdate update = new PlayerUpdate(this);
        update.updateDisplayName(displayName);

        Main.SQL.updatePlayerData(update);

        this.displayName = displayName;
    }

    /**
     * Update this player's state for all other players
     * @throws IOException If there was a problem sending the packets
     */
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

    private void updatePlayerStateFor(Player p) throws IOException {
        PlayerStatePacket packet = new PlayerStatePacket(p.getClient());
        packet.writePacket(this);
    }

    @Override
    public void updateState() throws IOException {
        if (!isInMatch() || !isUDPConnected())
            return;

        boolean visible = isVisible();

        if ((!visible && oldVisibleState) || visible) {
            for (Player opp : getOpponents()) {
                this.updateStateFor(opp); //Update this state for the opponent
            }

            if (!visible && invisiblePacketCount > MAX_INVISIBLE_PACKET_COUNT) {
                oldVisibleState = false;
            } else if (!visible && invisiblePacketCount <= MAX_INVISIBLE_PACKET_COUNT) {
                invisiblePacketCount++;
            } else if (visible) {
                oldVisibleState = true;
                invisiblePacketCount = 0;
            }
        }

        for (Player ally : getTeam().getTeamMembers()) { //This loop will include all allies and this player
            ally.updateStateFor(this);
        }
    }

    /**
     * Spawn an entity for this player
     * @param entity The entity to spawn
     * @throws IOException Whether there was a problem sending the packet
     * @throws java.lang.IllegalStateException If the player is not connected via UDP
     */
    public void spawnEntity(Entity entity) throws IOException {
        spawnEntity(entity, false);
    }

    /**
     * Spawn an entity for this player
     * @param entity The entity to spawn
     * @param force Whether the player should be connected via UDP
     * @throws IOException Whether there was a problem sending the packet
     * @throws java.lang.IllegalStateException If the player is not connected via UDP and <b>force</b> is false
     */
    public void spawnEntity(Entity entity, boolean force) throws IOException {
        if (!isUDPConnected() && !force)
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

    /**
     * Despawn an entity for this player
     * @param e The entity to despawn
     * @throws IOException If there was a problem sending the packet
     * @throws java.lang.IllegalStateException If the player is not connected via UDP
     */
    public void despawnEntity(Entity e) throws IOException {
        if (!isUDPConnected())
            throw new IllegalStateException("This client is not connected!");

        DespawnEntityPacket packet = new DespawnEntityPacket(client);
        packet.writePacket(e);
    }

    @Override
    public String getName() {
        return getDisplayName();
    }

    @Override
    public void setName(String name) { }

    /**
     * Have this player move towards an {x, y} point and update all players in the match
     * @param targetX The x point to move towards
     * @param targetY The y point to move towards
     */
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

    /**
     * Have this player fire towards an {x, y} point and update all players in the match
     * @param targetX The x point to fire towards
     * @param targetY The y point to fire towards
     */
    public void fireTowards(float targetX, float targetY) {
        if (!isUDPConnected() || System.currentTimeMillis() - lastFire < 300)
            return;

        lastActive = System.currentTimeMillis();

        lastFire = System.currentTimeMillis();
        didFire = true;
        if (!isVisible()) {
            setVisible(true);
            visibleTime = calculateVisibleTime();
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

    /**
     * Get all the opponents of this player.
     * @return All {@link me.eddiep.ghost.server.game.entities.Player} objects that are opponents to this player
     */
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

    /**
     * Get all allies of this player
     * @return All {@link me.eddiep.ghost.server.game.entities.Player} objects that are allies to this player
     */
    public Player[] getAllies() {
        if (getTeam() == null)
            return new Player[0];

        return getTeam().getTeamMembers();
    }

    /**
     * Whether or not this player is currently moving towards a point
     * @return True if the player is moving towards a point, otherwise false
     */
    public boolean hasTarget() {
        return target != null;
    }

    /**
     * Get the point this player is currently moving towards
     * @return The point this player is moving towards
     */
    public Vector2f getTarget() {
        return target;
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
            if (isVisible() && System.currentTimeMillis() - lastFire >= visibleTime) {
                setVisible(false);
                didFire = false;
            }
        } else if (wasHit) {
            if (isVisible() && System.currentTimeMillis() - lastHit >= visibleTime) {
                setVisible(false);
                wasHit = false;
            }
        }

        if (trackingMatchStats != null)
            trackingMatchStats.tick();

        super.tick();
    }

    private void handleVisibleState() {

    }

    private long calculateVisibleTime() {
        long duration = System.currentTimeMillis() - lastFire;

        return (long)FastMatch.pow(Math.log(duration), 5.4) + 800L;
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

    /**
     * The client for this player disconnected. <b>THIS SHOULD ONLY BE CALLED FROM THE {@link me.eddiep.ghost.server.network.Client} CLASS!</b>
     */
    public void disconnected() {
        client = null;

        lastActive = System.currentTimeMillis();
    }

    /**
     * Get the amount of shots hit a player
     * @return The amount of shots that hit a player
     */
    public long getShotsHit() {
        return shotsHit;
    }

    /**
     * Get the amount of shots missed a player
     * @return The amount of shots that missed
     */
    public long getShotsMissed() {
        return shotsMissed;
    }

    /**
     * Get a list of player id's that this player has killed
     * @return A list of player id's that this player killed. An ID is never repeated
     */
    public Set<Long> getPlayersKilled() {
        return playersKilled;
    }

    /**
     * Get the amount of hat tricks this player has done (ooo sexy)
     * @return The amount of hat tricks this player has done overall
     */
    public int getHatTrickCount() {
        return hatTricks;
    }

    /**
     * Get the {@link me.eddiep.ghost.server.game.ranking.Rank} object associated with this player
     * @return The {@link me.eddiep.ghost.server.game.ranking.Rank} for this player
     */
    public Rank getRanking() {
        return ranking;
    }

    /**
     * Get the friend list for this player.
     * @return A {@link java.util.Set} of player ids that represent the player's friend list
     */
    public Set<Long> getFriendIds() {
        return friends;
    }

    /**
     * Get a list of {@link me.eddiep.ghost.server.game.entities.Player} objects of currently online friends
     * @return A {@link java.util.List} of currently online friends
     */
    public List<Player> getOnlineFriends() {
        ArrayList<Player> toReturn = new ArrayList<>();
        for (long l : friends) {
            Player p = PlayerFactory.findPlayerById(l);
            if (p != null)
                toReturn.add(p);
        }

        return toReturn;
    }

    /**
     * Get a list of {@link me.eddiep.ghost.server.network.sql.PlayerData} objects of currently online friends
     * @return A {@link java.util.List} of stats of currently online friends
     */
    public List<PlayerData> getOnlineFriendsStats() {
        ArrayList<PlayerData> toReturn = new ArrayList<>();
        for (long l : friends) {
            Player p = PlayerFactory.findPlayerById(l);
            if (p != null)
                toReturn.add(p.getStats());
        }

        return toReturn;
    }

    /**
     * Get the stats of this player
     * @return The stats of this player represented as a {@link me.eddiep.ghost.server.network.sql.PlayerData} object
     */
    public PlayerData getStats() {
        return new PlayerData(this);
    }

    /**
     * Send a notification to this player
     * @param title The title of the notification
     * @param description The description of the notification
     */
    public void sendNotification(String title, String description) {
        NotificationBuilder.newNotification(this)
                .title(title)
                .description(description)
                .build()
                .send();
    }

    /**
     * Send a request to this player
     * @param title The title of the request
     * @param description The description of the request
     * @param result The callback for when the client responds
     */
    public void sendRequest(String title, String description, final PRunnable<Boolean> result) {
        NotificationBuilder.newNotification(this)
                .title(title)
                .description(description)
                .buildRequest()
                .onResponse(new PRunnable<Request>() {
                    @Override
                    public void run(Request p) {
                        result.run(p.accepted());
                    }
                })
                .send();
    }


    /**
     * Create a request from <b>p</b> to be friends with this player
     * @param p The player where the request came from
     */
    public void requestFriend(Player p) {
        if (friends.contains(p.getPlayerID()))
            return;

        final Request request = NotificationBuilder.newNotification(p)
                .title("Friend Request")
                .description(getDisplayName() + " would like to add you as a friend!")
                .buildRequest();

        request.onResponse(new PRunnable<Request>() {
            @Override
            public void run(Request p) {
                if (request.accepted()) {
                    friends.add(p.getTarget().getPlayerID());
                    p.getTarget().friends.add(getPlayerID());
                }
            }
        }).send();
    }

    /**
     * Send a notification to this player
     * @param notification The notification object to send
     */
    public void sendNewNotification(Notification notification) {
        while (requests.containsKey(notification.getId())) {
            notification.regenerateId();
        }

        if (notification instanceof Request) {
            requests.put(notification.getId(), (Request)notification);
        }

        if (client != null) {
            NewNotificationPacket packet = new NewNotificationPacket(client);
            try {
                packet.writePacket(notification);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Respond to a request and update the client
     * @param id The ID of the request to respond to
     * @param value The response to the request
     */
    public void respondToRequest(int id, boolean value) {
        Request request = requests.get(id);
        if (request.expired())
            return;

        request.respond(value);

        requests.remove(id);
    }

    /**
     * Remove a request from the client
     * @param request The request object to remove
     */
    public void removeRequest(Request request) {
        requests.remove(request.getId());

        if (client != null) {
            DeleteRequestPacket packet = new DeleteRequestPacket(client);
            try {
                packet.writePacket(request);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Logout this player and invalidate its session
     * @throws IOException If there was a problem disconnecting the client (wat)
     */
    public void logout() throws IOException {
        PlayerFactory.invalidateSession(this);
        if (client != null) {
            client.getServer().disconnect(client);
        }
    }
}
