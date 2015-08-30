package me.eddiep.ghost.matchmaking.player;

import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.game.ranking.Rank;
import me.eddiep.ghost.game.ranking.Rankable;
import me.eddiep.ghost.matchmaking.network.PlayerClient;
import me.eddiep.ghost.matchmaking.network.database.Database;
import me.eddiep.ghost.matchmaking.network.packets.DeleteRequestPacket;
import me.eddiep.ghost.matchmaking.network.packets.NewNotificationPacket;
import me.eddiep.ghost.matchmaking.queue.PlayerQueue;
import me.eddiep.ghost.network.notifications.Notifiable;
import me.eddiep.ghost.network.notifications.Notification;
import me.eddiep.ghost.network.notifications.NotificationBuilder;
import me.eddiep.ghost.network.notifications.Request;
import me.eddiep.ghost.network.sql.PlayerData;
import me.eddiep.ghost.network.sql.PlayerUpdate;
import me.eddiep.ghost.utils.PRunnable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

public class Player implements Notifiable, Rankable {

    private PlayerQueue queue;
    private String session;
    private PlayerData sqlData;
    private String username;
    private PlayerClient client;

    //===SQL DATA===
    long shotsHit;
    long shotsMissed;
    int hatTricks;
    protected long pid;
    protected String displayName;
    Set<Long> playersKilled;
    protected Rank ranking;
    protected Set<Long> friends;
    //===SQL DATA===

    protected HashMap<Integer, Request> requests = new HashMap<>();
    private boolean isInMatch;

    Player(String session, PlayerData sqlData) {
        this.session = session;
        this.sqlData = sqlData;
        this.username = sqlData.getUsername();
        this.ranking = Database.getRank(sqlData.getId());
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
        PlayerUpdate update = new PlayerUpdate(sqlData);

        update.updateShotsMade(shotsHit);
        update.updateShotsMissed(shotsMissed);
        update.updatePlayersKilled(playersKilled);
        update.updateHatTricks(hatTricks);
        update.updateRank(ranking);

        //update.push();

        sqlData = update.complete();
    }

    public PlayerData getStats() {
        return sqlData;
    }

    public boolean isInQueue() {
        return queue != null;
    }

    public PlayerClient getClient() {
        return client;
    }

    public void setClient(PlayerClient client) {
        if (this.client != null)
            throw new IllegalStateException("This player already has a client!");

        this.client = client;
    }

    public void disconnected() {
        client = null;
    }

    public long getPlayerID() {
        return sqlData.getId();
    }

    public boolean isInMatch() {
        return isInMatch;
    }

    public void setQueue(PlayerQueue queue) {
        this.queue = queue;
    }

    public PlayerQueue getQueue() {
        return queue;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return sqlData.getDisplayname();
    }

    public String getSession() {
        return session;
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
    public void requestFriend(final Player p) {
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
        if (client != null) {
            client.getServer().disconnect(client);
        }
    }

    public void setInMatch(boolean isInMatch) {
        this.isInMatch = isInMatch;
    }

    public Rank getRanking() {
        return ranking;
    }
}
