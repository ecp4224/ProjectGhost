package com.boxtrotstudio.ghost.game.match.entities.playable.impl;

import com.boxtrotstudio.ghost.game.match.LiveMatch;
import com.boxtrotstudio.ghost.game.match.entities.NetworkEntity;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.entities.playable.BasePlayableEntity;
import com.boxtrotstudio.ghost.game.match.world.World;
import com.boxtrotstudio.ghost.game.queue.Queues;
import com.boxtrotstudio.ghost.network.Client;
import com.boxtrotstudio.ghost.network.Server;
import com.boxtrotstudio.ghost.network.notifications.Notifiable;
import com.boxtrotstudio.ghost.network.notifications.Notification;
import com.boxtrotstudio.ghost.network.notifications.NotificationBuilder;
import com.boxtrotstudio.ghost.network.notifications.Request;
import com.boxtrotstudio.ghost.network.sql.PlayerData;
import com.boxtrotstudio.ghost.network.sql.PlayerUpdate;
import com.boxtrotstudio.ghost.utils.Global;
import com.boxtrotstudio.ghost.utils.PRunnable;
import com.boxtrotstudio.ghost.utils.Vector2f;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

public abstract class BaseNetworkPlayer<T extends Server, C extends Client<T>> extends BasePlayableEntity
        implements NetworkEntity, Notifiable {
    protected String username;
    protected String session;
    protected C client;
    protected int lastRecordedTick;

    public long lastActive;
    protected long logonTime;

    protected HashMap<Integer, Request> requests = new HashMap<>();

    //===SQL DATA===
    long shotsHit;
    long shotsMissed;
    int hatTricks;
    protected long pid;
    protected String displayName;
    Set<Long> playersKilled;
    protected Set<Long> friends;
    //===SQL DATA===

    protected BaseNetworkPlayer(String username, String session, PlayerData sqlData) {
        this.username = username;
        this.session = session;
        this.logonTime = this.lastActive = System.currentTimeMillis();
        this.loadSQLData(sqlData);
    }

    protected void loadSQLData(PlayerData sqlData) {
        pid = sqlData.getId();
        shotsHit = sqlData.getShotsHit();
        shotsMissed = sqlData.getShotsMissed();
        displayName = sqlData.getUsername();
        playersKilled = sqlData.getPlayersKilled();
        friends = sqlData.getFriends();
    }


    protected void saveSQLData(Queues type, boolean won, int value) {
        PlayerUpdate update = new PlayerUpdate(this);


        update.updateShotsMade(shotsHit);
        update.updateShotsMissed(shotsMissed);
        update.updatePlayersKilled(playersKilled);

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
    public String getSession() {
        return session;
    }

    @Override
    public boolean isConnected() {
        return client != null && client.getPort() != -1;
    }

    /**
     * Get the currently connected client for this playable.
     * @return The currently connected {@link Client}
     */
    public C getClient() {
        return client;
    }

    @Override
    public void onDamagePlayable(PlayableEntity hit) {
        super.onDamagePlayable(hit);
        shotsHit++;
    }

    @Override
    public void onKilledPlayable(PlayableEntity killed) {
        super.onKilledPlayable(killed);

        if (killed instanceof BaseNetworkPlayer)
            playersKilled.add(((BaseNetworkPlayer) killed).getPlayerID());
    }

    /**
     * Check if this playable is connected via UDP.
     * @return True if this playable is, otherwise false
     */
    public boolean isUDPConnected() {
        return client != null && client.getPort() != -1;
    }

    /**
     * Set the client for this playable. <b>This method should only be invoked by the {@link Client} class!</b>
     * @param c The new client
     */
    public void setClient(C c) {
        if (this.client != null)
            throw new IllegalStateException("This Player already has a client!");


        this.client = c;
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
        if (Global.SQL != null) {
            PlayerUpdate update = new PlayerUpdate(this);
            update.updateDisplayName(displayName);

            Global.SQL.updatePlayerData(update);
        }

        this.displayName = displayName;
    }

    @Override
    public String getName() {
        return getDisplayName();
    }

    @Override
    public void setName(String name) {
        setDisplayName(name);
    }

    /**
     * Have this playable move towards an {x, y} point and update all players in the match
     * @param targetX The x point to move towards
     * @param targetY The y point to move towards
     */
    public void moveTowards(float targetX, float targetY) {
        if (!isUDPConnected())
            return;
        if (frozen || isDead)
            return;

        lastActive = System.currentTimeMillis();

        float x = position.x;
        float y = position.y;

        float asdx = targetX - x;
        float asdy = targetY - y;
        float inv = (float) Math.atan2(asdy, asdx);



        velocity.x = (float) (Math.cos(inv)*super.speed.getValue());
        velocity.y = (float) (Math.sin(inv)*super.speed.getValue());

        target = new Vector2f(targetX, targetY);

        World world = getWorld();
        if (world != null) {
            world.requestEntityUpdate();
        }
    }

    /**
     * Have this playable move in a given direction
     * @param direction A normalized vector representing the direction to move in
     */
    public void moveWithDirection(Vector2f direction) {
        if (!isUDPConnected())
            return;
        if (frozen || isDead)
            return;

        lastActive = System.currentTimeMillis();

        velocity.x = direction.x * getSpeed();
        velocity.y = direction.y * getSpeed();

        target = null;

        World world = getWorld();
        if (world != null) {
            world.requestEntityUpdate();
        }
    }

    /**
     * Have this playable fire towards an {x, y} point and update all players in the match
     * @param targetX The x point to fire towards
     * @param targetY The y point to fire towards
     * @param action The action that was requested
     */
    public void fireTowards(float targetX, float targetY, int action) {
        if (!isUDPConnected())
            return;

        lastActive = System.currentTimeMillis();

        useAbility(targetX, targetY, action);
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
     * The client for this playable disconnected. <b>THIS SHOULD ONLY BE CALLED FROM THE {@link Client} CLASS!</b>
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
     * Get the friend list for this playable.
     * @return A {@link java.util.Set} of playable ids that represent the playable's friend list
     */
    public Set<Long> getFriendIds() {
        return friends;
    }

    /**
     * Get the stats of this playable
     * @return The stats of this playable represented as a {@link PlayerData} object
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
