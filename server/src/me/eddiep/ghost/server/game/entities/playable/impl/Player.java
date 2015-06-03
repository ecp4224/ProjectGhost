package me.eddiep.ghost.server.game.entities.playable.impl;

import me.eddiep.ghost.server.Main;
import me.eddiep.ghost.server.game.ActiveMatch;
import me.eddiep.ghost.server.game.Match;
import me.eddiep.ghost.server.game.entities.playable.BasePlayableEntity;
import me.eddiep.ghost.server.game.entities.playable.Playable;
import me.eddiep.ghost.server.game.queue.PlayerQueue;
import me.eddiep.ghost.server.game.queue.Queues;
import me.eddiep.ghost.server.game.ranking.Rank;
import me.eddiep.ghost.server.game.stats.TemporaryStats;
import me.eddiep.ghost.server.game.stats.TrackingMatchStats;
import me.eddiep.ghost.server.game.util.*;
import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.network.packet.impl.DeleteRequestPacket;
import me.eddiep.ghost.server.network.packet.impl.NewNotificationPacket;
import me.eddiep.ghost.server.network.sql.PlayerData;
import me.eddiep.ghost.server.network.sql.PlayerUpdate;
import me.eddiep.ghost.server.utils.PRunnable;

import java.io.IOException;
import java.util.*;

import static me.eddiep.ghost.server.utils.Constants.*;

public class Player extends BasePlayableEntity {
    public static final int WIDTH = 48;
    public static final int HEIGHT = 48;
    private static final float SPEED = 6f;
    private static final float BULLET_SPEED = 12f;
    private static final float VISIBLE_TIMER = 800f;


    private TrackingMatchStats trackingMatchStats;
    private String username;
    private UUID session;
    private Client client;
    private PlayerQueue queue;
    private int lastRecordedTick;
    private Vector2f target;
    boolean wasHit;
    long lastHit;
    int hatTrickCount;

    private long lastActive;
    private long logonTime;

    private HashMap<Integer, Request> requests = new HashMap<>();

    private VisibleFunction function = VisibleFunction.ORGINAL;

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
    private TemporaryStats tempStats;
    //===SQL DATA===


    /**
     * Create a new user with the provided username and SQL Data
     * @param username The username of this playable
     * @param sqlData The SQL data for associated with this playable
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

    public void setVisibleFunction(VisibleFunction function) {
        this.function = function;
    }

    public VisibleFunction getVisibleFunction() {
        return function;
    }

    @Override
    public void prepareForMatch() {
        oldVisibleState = true;
        setVisible(false);
        resetUpdateTimer();
    }

    @Override
    public void onDamage(Playable damager) {
        wasHit = true;

        lastHit = System.currentTimeMillis();
        hatTrickCount = 0; //If you get hit, then reset hit hatTrickCount
        switch (function) {
            case ORGINAL:
                if (!isVisible())
                    setVisible(true);
                break;
            case TIMER:
                if (visibleIndicator < VISIBLE_COUNTER_DEFAULT_LENGTH) {
                    visibleIndicator = VISIBLE_COUNTER_DEFAULT_LENGTH;
                }
                break;
        }
    }

    @Override
    public void onFire() {
        tempStats.plusOne(TemporaryStats.SHOTS_FIRED);

        lastFire = System.currentTimeMillis();
        didFire = true;
        switch (function) {
            case ORGINAL:
                if (!isVisible())
                    setVisible(true);
                break;
            case TIMER:
                if (visibleIndicator < VISIBLE_COUNTER_DEFAULT_LENGTH) {
                    visibleIndicator = VISIBLE_COUNTER_DEFAULT_LENGTH;
                }
                break;
        }

    }

    @Override
    public void onDamagePlayable(Playable hit) {
        tempStats.plusOne(TemporaryStats.SHOTS_HIT);
        shotsHit++;
        hatTrickCount++;
        if (hatTrickCount > 0 && hatTrickCount % 3 == 0) { //If the shooter's hatTrickCount is a multiple of 3
            hatTricks++; //They got a hat trick
            tempStats.plusOne(TemporaryStats.HAT_TRICKS);
        }
    }

    @Override
    public void onKilledPlayable(Playable killed) {
        if (killed instanceof Player)
            playersKilled.add(((Player)killed).getPlayerID());
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
     * Set the client for this playable. <b>This method should only be invoked by the {@link me.eddiep.ghost.server.network.Client} class!</b>
     * @param c The new client
     */
    public void setClient(Client c) {
        if (this.client != null)
            throw new IllegalStateException("This Player already has a client!");


        this.client = c;
    }

    /**
     * Check whether or not this playable is currently waiting in queue
     * @return True if the playable is waiting in queue, otherwise false
     */
    public boolean isInQueue() {
        return queue != null;
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

    @Override
    public TemporaryStats getCurrentMatchStats() {
        return null;
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
    public void setMatch(ActiveMatch containingMatch) {
        super.setMatch(containingMatch);
        lastActive = System.currentTimeMillis();

        if (containingMatch != null) {
            trackingMatchStats = new TrackingMatchStats(this);
            tempStats = new TemporaryStats();
        }
    }

    @Override
    public void onWin(Match match) {
        int val;

        if (winHash.containsKey(match.queueType().asByte())) {
            val = winHash.get(match.queueType().asByte());
            val++;
            winHash.put(match.queueType().asByte(), val);
        } else {
            winHash.put(match.queueType().asByte(), 1);
            val = 1;
        }

        saveSQLData(match.queueType(), true, val);
    }

    @Override
    public void onLose(Match match) {
        int val;
        if (loseHash.containsKey(match.queueType().asByte())) {
            val = loseHash.get(match.queueType().asByte());
            val++;
            loseHash.put(match.queueType().asByte(), val);
        } else {
            loseHash.put(match.queueType().asByte(), 1);
            val = 1;
        }

        saveSQLData(match.queueType(), false, val);
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

        Main.SQL.updatePlayerData(update);

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

        useAbility(targetX, targetY);
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

        if (function == VisibleFunction.TIMER) {
            if (getMatch().hasMatchStarted()) {
                handleVisibleState();
            }
        } else if (function == VisibleFunction.ORGINAL) {
            if (didFire) {
                if (isVisible() && System.currentTimeMillis() - lastFire >= VISIBLE_TIMER) {
                    setVisible(false);
                    didFire = false;
                }
            } else if (wasHit) {
                if (isVisible() && System.currentTimeMillis() - lastHit >= VISIBLE_TIMER) {
                    setVisible(false);
                    wasHit = false;
                }
            }
        }

        if (trackingMatchStats != null)
            trackingMatchStats.tick();

        super.tick();
    }

    public int getVisibleIndicatorPosition() {
        return visibleIndicator;
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
     * Get the stats of this playable
     * @return The stats of this playable represented as a {@link me.eddiep.ghost.server.network.sql.PlayerData} object
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
     * @throws IOException If there was a problem disconnecting the client (wat)
     */
    public void logout() throws IOException {
        PlayerFactory.invalidateSession(this);
        if (client != null) {
            client.getServer().disconnect(client);
        }
    }
}
