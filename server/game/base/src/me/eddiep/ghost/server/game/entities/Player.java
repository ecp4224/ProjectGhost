package me.eddiep.ghost.server.game.entities;

import static me.eddiep.ghost.server.utils.Constants.*;

import me.eddiep.ghost.server.Starter;
import me.eddiep.ghost.server.game.ActiveMatch;
import me.eddiep.ghost.server.game.Entity;
import me.eddiep.ghost.server.game.entities.playable.impl.PlayerFactory;
import me.eddiep.ghost.server.game.queue.PlayerQueue;
import me.eddiep.ghost.server.game.ranking.Rank;
import me.eddiep.ghost.server.game.stats.TrackingMatchStats;
import me.eddiep.ghost.server.game.team.Team;
import me.eddiep.ghost.server.game.util.*;
import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.network.dataserv.PlayerData;
import me.eddiep.ghost.server.network.packet.impl.*;
import me.eddiep.ghost.server.utils.Notification;
import me.eddiep.ghost.server.utils.NotificationBuilder;
import me.eddiep.ghost.server.utils.PRunnable;
import me.eddiep.ghost.server.utils.Request;

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

    private HashMap<Integer, Request> requests = new HashMap<>();

    private long lastActive;
    private long logonTime;

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
     * @param sqlData The SQL data for associated with this playable
     * @return A new {@link me.eddiep.ghost.server.game.entities.playable.impl.Player} object
     */
    static Player createPlayer(PlayerData sqlData) {
        Player player = new Player();
        player.username = sqlData.getUsername();
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

    void saveSQLData() {
        Starter.getLoginBridge().updatePlayerStats(getSession().toString(), getStats());
    }

    /**
     * Get the total amount of bullets fired by this playable
     * @return The total amount of bullets fired
     */
    public long getTotalShotsFired() {
        return shotsHit + shotsMissed;
    }

    /**
     * Calculate how accurate this playable's shots are overall
     * @return The accuracy of this playable overall
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
     * Get the playable id of this playable. This is unique per playable
     * @return The unique playable id for this playable
     */
    public long getPlayerID() {
        return pid;
    }

    /**
     * Get the username for this playable
     * @return The username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Get this playable's current session.
     * @return The current session for this playable
     */
    public UUID getSession() {
        return session;
    }

    /**
     * Get the currently connected client for this playable.
     * @return The currently connected {@link me.eddiep.ghost.server.network.Client}
     */
    public Client getClient() {
        return client;
    }

    /**
     * Check if this playable is connected via UDP.
     * @return True if this playable is, otherwise false
     */
    public boolean isUDPConnected() {
        return client != null && client.getPort() != -1;
    }

    /**
     * Set the client for this playable. <b>This method should only be invoked by the {@link me.eddiep.ghost.server.network.Client} class!</b>
     * @param c The new client
     */
    public void setClient(Client c) {
        if (this.client != null)
            throw new IllegalStateException("This Player already has a client!");


        this.client = c;
    }

    /**
     * Get the team this playable is on. If this playable is not in a match, then null is returned.
     * @return The team for this playable
     */
    public Team getTeam() {
        return containingMatch == null ? null : containingMatch.getTeamFor(this);
    }

    /**
     * Check whether or not this playable is currently waiting in queue
     * @return True if the playable is waiting in queue, otherwise false
     */
    public boolean isInQueue() {
        return queue != null;
    }

    /**
     * Check whether or not this playable is currently in a match
     * @return True if the playable is in a match, otherwise false
     */
    public boolean isInMatch() {
        return getMatch() != null;
    }

    /**
     * Check whether this playable is dead
     * @return True if the playable is dead, otherwise false
     */
    public boolean isDead() {
        return isDead;
    }

    /**
     * Get the amount of lives this playable has
     * @return The amount of lives as a byte
     */
    public byte getLives() {
        return lives;
    }

    /**
     * Subtract 1 life from this playable and update all other players
     *
     * <b>The playable must be in a match, otherwise a {@link IllegalStateException} exception will be thrown</b>
     */
    public void subtractLife() {
        if (!isInMatch())
            throw new IllegalStateException("This playable is not in a match!");

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
     * Add 1 life to this playable and update all other players
     *
     * <b>The playable must be in a match, otherwise a {@link IllegalStateException} exception will be thrown</b>
     */
    public void addLife() {
        if (!isInMatch())
            throw new IllegalStateException("This playable is not in a match!");

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
     * Reset this playable's lives and update all other players
     *
     * <b>The playable must be in a match, otherwise a {@link IllegalStateException} exception will be thrown</b>
     */
    public void resetLives() {
        if (!isInMatch())
            throw new IllegalStateException("This playable is not in a match!");

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
     * Kill this playable and update all other players
     *
     * <b>The playable must be in a match, otherwise a {@link IllegalStateException} exception will be thrown</b>
     */
    public void kill() {
        if (!isInMatch())
            throw new IllegalStateException("This playable is not in a match!");

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
     * Freeze this playable and update all other players
     *
     * <b>The playable must be in a match, otherwise a {@link IllegalStateException} exception will be thrown</b>
     */
    public void freeze() {
        if (!isInMatch())
            throw new IllegalStateException("This playable is not in a match!");

        frozen = true;
        try {
            updatePlayerState();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Unfreeze this playable and update all other players
     *
     * <b>The playable must be in a match, otherwise a {@link IllegalStateException} exception will be thrown</b>
     */
    public void unfreeze() {
        if (!isInMatch())
            throw new IllegalStateException("This playable is not in a match!");

        frozen = false;
        try {
            updatePlayerState();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check whether this playable is frozen or not.
     * @return True if the playable is frozen, otherwise false
     */
    public boolean isFrozen() {
        return frozen;
    }

    /**
     * Get the current queue this playable is waiting in
     * @return The current queue this playable is waiting in
     */
    public PlayerQueue getQueue() {
        return queue;
    }

    /**
     * Set the queue this playable is currently waiting in <b>This method should only be called by {@link me.eddiep.ghost.server.game.queue.PlayerQueue} objects!</b>
     * @param queue The queue the playable joined
     */
    public void setQueue(PlayerQueue queue) {
        this.queue = queue;
        lastActive = System.currentTimeMillis();
    }

    /**
     * Get the {@link me.eddiep.ghost.server.game.stats.TrackingMatchStats} object for this playable
     * @return The {@link me.eddiep.ghost.server.game.stats.TrackingMatchStats} object for this playable
     */
    public TrackingMatchStats getTrackingStats() {
        return trackingMatchStats;
    }

    /**
     * Get the last time this playable was active as a unix timestamp.
     * @return The last time this playable was active
     * @see System#currentTimeMillis()
     */
    public long getLastActiveTime() {
        return lastActive;
    }

    /**
     * Get the time this playable logged in as a unix timestamp
     * @return The time this playable logged in
     * @see System#currentTimeMillis()
     */
    public long getLogonTime() {
        return logonTime;
    }

    /**
     * Get how long this playable has been logged into the current session
     * @return The duration this session has been active
     * @see System#currentTimeMillis()
     */
    public long getLoginDuration() {
        return System.currentTimeMillis() - logonTime;
    }

    /**
     * Get how long it has been since the playable has done something
     * @return The duration since the playable last did something
     * @see System#currentTimeMillis()
     */
    public long getLastActionDuration() {
        return System.currentTimeMillis() - lastActive;
    }

    /**
     * Check to see if this playable is ready
     * @return True if the playable is ready, otherwise false
     */
    public boolean isReady() {
        return isReady;
    }

    /**
     * Set this playable's ready state <b>THIS DOES NOT UPDATE THE CLIENT. THIS METHOD SHOULD ONLY BE CALLED FROM {@link me.eddiep.ghost.server.network.packet.impl.ReadyPacket}</b>
     * @param isReady Whether this playable is ready
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
     * Get the displayname of this playable
     * @return The displayname
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Set the displayname of this playable and save it. <b>THIS DOES NOT UPDATE THE CLIENT</b>
     * @param displayName The new displayname
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        Starter.getLoginBridge().updatePlayerStats(getSession().toString(), getStats());
    }

    /**
     * Update this playable's state for all other players
     * @throws java.io.IOException If there was a problem sending the packets
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

        //boolean visible = isVisible();

        /*if ((!visible && oldVisibleState) || visible) {
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
        }*/

        if (alpha > 0 || (alpha == 0 && oldVisibleState)) {

            for (Player opp : getOpponents()) {
                this.updateStateFor(opp);
            }

            oldVisibleState = alpha != 0;
        }

        for (Player ally : getTeam().getTeamMembers()) { //This loop will include all allies and this playable
            ally.updateStateFor(this);
        }
    }

    /**
     * Spawn an entity for this playable
     * @param entity The entity to spawn
     * @throws java.io.IOException Whether there was a problem sending the packet
     * @throws IllegalStateException If the playable is not connected via UDP
     */
    public void spawnEntity(Entity entity) throws IOException {
        spawnEntity(entity, false);
    }

    /**
     * Spawn an entity for this playable
     * @param entity The entity to spawn
     * @param force Whether the playable should be connected via UDP
     * @throws java.io.IOException Whether there was a problem sending the packet
     * @throws IllegalStateException If the playable is not connected via UDP and <b>force</b> is false
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
     * Despawn an entity for this playable
     * @param e The entity to despawn
     * @throws java.io.IOException If there was a problem sending the packet
     * @throws IllegalStateException If the playable is not connected via UDP
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
     * Have this playable move towards an {x, y} point and update all players in the match
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
     * Have this playable fire towards an {x, y} point and update all players in the match
     * @param targetX The x point to fire towards
     * @param targetY The y point to fire towards
     */
    public void fireTowards(float targetX, float targetY) {
        if (!isUDPConnected() || System.currentTimeMillis() - lastFire < 300)
            return;

        lastActive = System.currentTimeMillis();

        lastFire = System.currentTimeMillis();
        didFire = true;
        if (visibleIndicator < VISIBLE_COUNTER_DEFAULT_LENGTH) {
            visibleIndicator = VISIBLE_COUNTER_DEFAULT_LENGTH;
        }

        float x = position.x;
        float y = position.y;

        float asdx = targetX - x;
        float asdy = targetY - y;
        float inv = (float) Math.atan2(asdy, asdx);

        Vector2f velocity = new Vector2f((float)Math.cos(inv)*BULLET_SPEED, (float)Math.sin(inv)*BULLET_SPEED);

        BulletEntity b = new BulletEntity(this);
        b.setPosition(getPosition().cloneVector());
        b.setVelocity(velocity);

        try {
            getMatch().spawnEntity(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get all the opponents of this playable.
     * @return All {@link Player} objects that are opponents to this playable
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
     * Get all allies of this playable
     * @return All {@link Player} objects that are allies to this playable
     */
    public Player[] getAllies() {
        if (getTeam() == null)
            return new Player[0];

        return getTeam().getTeamMembers();
    }

    /**
     * Whether or not this playable is currently moving towards a point
     * @return True if the playable is moving towards a point, otherwise false
     */
    public boolean hasTarget() {
        return target != null;
    }

    /**
     * Get the point this playable is currently moving towards
     * @return The point this playable is moving towards
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

        handleVisibleState();
        /*if (didFire) {
            if (isVisible() && System.currentTimeMillis() - lastFire >= visibleTime) {
                setVisible(false);
                didFire = false;
            }
        } else if (wasHit) {
            if (isVisible() && System.currentTimeMillis() - lastHit >= visibleTime) {
                setVisible(false);
                wasHit = false;
            }
        }*/

        if (trackingMatchStats != null)
            trackingMatchStats.tick();

        super.tick();
    }


    int visibleIndicator;
    private void handleVisibleState() {
        if (didFire || wasHit) {
            visibleIndicator -= VISIBLE_COUNTER_DECREASE_RATE;
            if (visibleIndicator <= 0) {
                visibleIndicator = 0;
                alpha = 0;
                didFire = false;
                wasHit = false;
            }
        } else {
            visibleIndicator += VISIBLE_COUNTER_INCREASE_RATE;
        }

        if (visibleIndicator < VISIBLE_COUNTER_START_FADE) {
            alpha = 0;
        } else if (visibleIndicator > VISIBLE_COUNTER_START_FADE && visibleIndicator < VISIBLE_COUNTER_FULLY_VISIBLE) {
            int totalDistance = VISIBLE_COUNTER_FADE_DISTANCE;
            int curDistance = visibleIndicator - VISIBLE_COUNTER_START_FADE;

            alpha = Math.max(Math.min((int) (((double)curDistance / (double)totalDistance) * 255.0), 255), 0);

        } else if (visibleIndicator > VISIBLE_COUNTER_FULLY_VISIBLE) {
            alpha = 255;
        }
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
     * The client for this playable disconnected. <b>THIS SHOULD ONLY BE CALLED FROM THE {@link me.eddiep.ghost.server.network.Client} CLASS!</b>
     */
    public void disconnected() {
        client = null;

        lastActive = System.currentTimeMillis();
    }

    /**
     * Get the amount of shots hit a playable
     * @return The amount of shots that hit a playable
     */
    public long getShotsHit() {
        return shotsHit;
    }

    /**
     * Get the amount of shots missed a playable
     * @return The amount of shots that missed
     */
    public long getShotsMissed() {
        return shotsMissed;
    }

    /**
     * Get a list of playable id's that this playable has killed
     * @return A list of playable id's that this playable killed. An ID is never repeated
     */
    public Set<Long> getPlayersKilled() {
        return playersKilled;
    }

    /**
     * Get the amount of hat tricks this playable has done (ooo sexy)
     * @return The amount of hat tricks this playable has done overall
     */
    public int getHatTrickCount() {
        return hatTricks;
    }

    /**
     * Get the {@link me.eddiep.ghost.server.game.ranking.Rank} object associated with this playable
     * @return The {@link me.eddiep.ghost.server.game.ranking.Rank} for this playable
     */
    public Rank getRanking() {
        return ranking;
    }

    /**
     * Get the friend list for this playable.
     * @return A {@link java.util.Set} of playable ids that represent the playable's friend list
     */
    public Set<Long> getFriendIds() {
        return friends;
    }

    /**
     * Get a list of {@link Player} objects of currently online friends
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
     * Get a list of {@link me.eddiep.ghost.server.network.dataserv.PlayerData} objects of currently online friends
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
     * Get the stats of this playable
     * @return The stats of this playable represented as a {@link me.eddiep.ghost.server.network.dataserv.PlayerData} object
     */
    public PlayerData getStats() {
        return new PlayerData(this);
    }

    /**
     * Send a notification to this playable
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
     * Send a request to this playable
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
     * Create a request from <b>p</b> to be friends with this playable
     * @param p The playable where the request came from
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
     * Send a notification to this playable
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
     * Logout this playable and invalidate its session
     * @throws java.io.IOException If there was a problem disconnecting the client (wat)
     */
    public void logout() throws IOException {
        PlayerFactory.invalidateSession(this);
        if (client != null) {
            client.getServer().disconnect(client);
        }
    }
}
