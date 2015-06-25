package me.eddiep.ghost.game.entities.playable.impl;

import me.eddiep.ghost.game.LiveMatch;
import me.eddiep.ghost.game.entities.NetworkEntity;
import me.eddiep.ghost.game.entities.PlayableEntity;
import me.eddiep.ghost.game.entities.playable.BasePlayableEntity;
import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.game.ranking.Rank;
import me.eddiep.ghost.game.stats.TemporaryStats;
import me.eddiep.ghost.game.stats.TrackingMatchStats;
import me.eddiep.ghost.network.Client;
import me.eddiep.ghost.network.Server;
import me.eddiep.ghost.network.notifications.Notifiable;
import me.eddiep.ghost.network.notifications.Notification;
import me.eddiep.ghost.network.notifications.NotificationBuilder;
import me.eddiep.ghost.network.notifications.Request;
import me.eddiep.ghost.network.sql.PlayerData;
import me.eddiep.ghost.network.sql.PlayerUpdate;
import me.eddiep.ghost.utils.Global;
import me.eddiep.ghost.utils.PRunnable;
import me.eddiep.ghost.utils.Vector2f;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public abstract class BaseNetworkPlayer<T extends Server, C extends Client<T>> extends BasePlayableEntity
        implements NetworkEntity, Notifiable {
    public static final int WIDTH = 48;
    public static final int HEIGHT = 48;


    protected TrackingMatchStats trackingMatchStats;
    protected String username;
    protected UUID session;
    protected C client;
    protected int lastRecordedTick;
    protected Vector2f target;
    int hatTrickCount;

    protected long lastActive;
    protected long logonTime;

    protected HashMap<Integer, Request> requests = new HashMap<>();

    //===SQL DATA===
    long shotsHit;
    long shotsMissed;
    int hatTricks;
    protected long pid;
    protected String displayName;
    Set<Long> playersKilled;
    protected Rank ranking;
    protected Set<Long> friends;
    protected TemporaryStats tempStats;
    //===SQL DATA===

    protected BaseNetworkPlayer(String username, UUID session, PlayerData sqlData) {
        this.username = username;
        this.session = session;
        this.logonTime = this.lastActive = System.currentTimeMillis();
        this.loadSQLData(sqlData);
    }

    protected void loadSQLData(PlayerData sqlData) {
        pid = sqlData.getId();
        shotsHit = sqlData.getShotsHit();
        shotsMissed = sqlData.getShotsMissed();
        displayName = sqlData.getDisplayname();
        playersKilled = sqlData.getPlayersKilled();
        hatTricks = sqlData.getHatTrickCount();
        ranking = sqlData.getRank();
        friends = sqlData.getFriends();
    }


    protected void saveSQLData(Queues type, boolean won, int value) {
        PlayerUpdate update = new PlayerUpdate(this);


        update.updateShotsMade(shotsHit);
        update.updateShotsMissed(shotsMissed);
        update.updatePlayersKilled(playersKilled);
        update.updateHatTricks(hatTricks);
        update.updateRank(ranking);

        update.push();
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

    @Override
    public boolean isConnected() {
        return client != null && client.getPort() != -1;
    }

    /**
     * Get the currently connected client for this playable.
     * @return The currently connected {@link me.eddiep.ghost.network.Client}
     */
    public C getClient() {
        return client;
    }

    @Override
    public void onDamage(PlayableEntity damager) {
        super.onDamage(damager);

        hatTrickCount = 0; //If you get hit, then reset hit hatTrickCount
    }

    @Override
    public void onFire() {
        super.onFire();

        tempStats.plusOne(TemporaryStats.SHOTS_FIRED);
    }

    @Override
    public void onDamagePlayable(PlayableEntity hit) {
        tempStats.plusOne(TemporaryStats.SHOTS_HIT);
        shotsHit++;
        hatTrickCount++;
        if (hatTrickCount > 0 && hatTrickCount % 3 == 0) { //If the shooter's hatTrickCount is a multiple of 3
            hatTricks++; //They got a hat trick
            tempStats.plusOne(TemporaryStats.HAT_TRICKS);
        }
    }

    @Override
    public void onKilledPlayable(PlayableEntity killed) {
        if (killed instanceof BaseNetworkPlayer)
            playersKilled.add(((BaseNetworkPlayer) killed).getPlayerID());
    }

    @Override
    public void onShotMissed() {
        shotsMissed++;
        tempStats.plusOne(TemporaryStats.SHOTS_MISSED);
    }

    /**
     * Check if this playable is connected via UDP.
     * @return True if this playable is, otherwise false
     */
    public boolean isUDPConnected() {
        return client != null && client.getPort() != -1;
    }

    /**
     * Set the client for this playable. <b>This method should only be invoked by the {@link me.eddiep.ghost.network.Client} class!</b>
     * @param c The new client
     */
    public void setClient(C c) {
        if (this.client != null)
            throw new IllegalStateException("This Player already has a client!");


        this.client = c;
    }

    /**
     * Get the {@link me.eddiep.ghost.game.stats.TrackingMatchStats} object for this playable
     * @return The {@link me.eddiep.ghost.game.stats.TrackingMatchStats} object for this playable
     */
    public TrackingMatchStats getTrackingStats() {
        return trackingMatchStats;
    }

    @Override
    public TemporaryStats getCurrentMatchStats() {
        return tempStats;
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

    @Override
    public void setMatch(LiveMatch containingMatch) {
        super.setMatch(containingMatch);
        lastActive = System.currentTimeMillis();

        if (containingMatch != null) {
            trackingMatchStats = new TrackingMatchStats(this);
            tempStats = new TemporaryStats();
        }
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
        PlayerUpdate update = new PlayerUpdate(this);
        update.updateDisplayName(displayName);

        Global.SQL.updatePlayerData(update);

        this.displayName = displayName;
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
        if (frozen)
            return;

        lastActive = System.currentTimeMillis();

        float x = position.x;
        float y = position.y;

        float asdx = targetX - x;
        float asdy = targetY - y;
        float inv = (float) Math.atan2(asdy, asdx);


        velocity.x = (float) (Math.cos(inv)*super.speed);
        velocity.y = (float) (Math.sin(inv)*super.speed);

        target = new Vector2f(targetX, targetY);

        getMatch().updateEntityState();
    }

    /**
     * Have this playable fire towards an {x, y} point and update all players in the match
     * @param targetX The x point to fire towards
     * @param targetY The y point to fire towards
     * @param action The action that was requested
     */
    public void fireTowards(float targetX, float targetY, int action) {
        if (!isUDPConnected() || System.currentTimeMillis() - lastFire < 300)
            return;

        lastActive = System.currentTimeMillis();

        useAbility(targetX, targetY, action);
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
                getMatch().updateEntityState();
            }
        }

        super.tick();

        if (trackingMatchStats != null)
            trackingMatchStats.tick();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BaseNetworkPlayer) {
            BaseNetworkPlayer p = (BaseNetworkPlayer)obj;
            if (p.getSession().equals(getSession()))
                return true;
        }
        return false;
    }

    /**
     * The client for this playable disconnected. <b>THIS SHOULD ONLY BE CALLED FROM THE {@link me.eddiep.ghost.network.Client} CLASS!</b>
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
     * Get the {@link me.eddiep.ghost.game.ranking.Rank} object associated with this playable
     * @return The {@link me.eddiep.ghost.game.ranking.Rank} for this playable
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
     * Get the stats of this playable
     * @return The stats of this playable represented as a {@link me.eddiep.ghost.network.sql.PlayerData} object
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
        NotificationBuilder.newNotificationFor(this)
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
        NotificationBuilder.newNotificationFor(this)
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
    public void requestFriend(final BaseNetworkPlayer p) {
        if (friends.contains(p.getPlayerID()))
            return;

        final Request request = NotificationBuilder.newNotificationFor(p)
                .title("Friend Request")
                .description(getDisplayName() + " would like to add you as a friend!")
                .buildRequest();

        request.onResponse(new PRunnable<Request>() {
            @Override
            public void run(Request req) {
                if (request.accepted()) {
                    friends.add(p.getPlayerID());
                    p.friends.add(getPlayerID());
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

        onSendNewNotification(notification);
    }

    protected void onSendNewNotification(Notification notification) { }

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

        onRemoveRequest(request);
    }

    protected void onRemoveRequest(Request request) { }

    /**
     * Logout this playable and invalidate its session
     * @throws java.io.IOException If there was a problem disconnecting the client (wat)
     */
    public void logout() throws IOException {
        if (client != null) {
            client.getServer().disconnect(client);
        }
    }
}
