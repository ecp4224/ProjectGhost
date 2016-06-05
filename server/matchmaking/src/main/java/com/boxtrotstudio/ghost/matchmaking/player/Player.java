package com.boxtrotstudio.ghost.matchmaking.player;

import com.boxtrotstudio.ghost.game.match.abilities.Ability;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.queue.Queues;
import com.boxtrotstudio.ghost.matchmaking.network.PlayerClient;
import com.boxtrotstudio.ghost.matchmaking.network.database.Database;
import com.boxtrotstudio.ghost.matchmaking.network.gameserver.Stream;
import com.boxtrotstudio.ghost.matchmaking.network.packets.DeleteRequestPacket;
import com.boxtrotstudio.ghost.matchmaking.network.packets.DisconnectReasonPacket;
import com.boxtrotstudio.ghost.matchmaking.network.packets.NewNotificationPacket;
import com.boxtrotstudio.ghost.matchmaking.player.ranking.Rank;
import com.boxtrotstudio.ghost.matchmaking.player.ranking.Rankable;
import com.boxtrotstudio.ghost.matchmaking.queue.PlayerQueue;
import com.boxtrotstudio.ghost.network.notifications.Notifiable;
import com.boxtrotstudio.ghost.network.notifications.Notification;
import com.boxtrotstudio.ghost.network.notifications.NotificationBuilder;
import com.boxtrotstudio.ghost.network.notifications.Request;
import com.boxtrotstudio.ghost.network.sql.PlayerData;
import com.boxtrotstudio.ghost.network.sql.PlayerUpdate;
import com.boxtrotstudio.ghost.utils.PRunnable;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Set;

public class Player implements Notifiable, Rankable, Comparable<Player> {

    private PlayerQueue queue;
    private String session;
    private PlayerData sqlData;
    private String username;
    private PlayerClient client;
    private long queueTime;
    private Stream stream;

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
    private InetAddress preferedServer;
    private Class currentAbility;
    private Ability<PlayableEntity> ability;

    Player(String session, PlayerData sqlData, Stream stream) {
        this.session = session;
        this.sqlData = sqlData;
        this.username = sqlData.getUsername();
        if (Database.isSetup()) {
            this.ranking = Database.getRank(sqlData.getId());
        }
        this.stream = stream;
        //this.ranking = Database.getRank(sqlData.getId());
    }

    protected void loadSQLData(PlayerData sqlData) {
        pid = sqlData.getId();
        shotsHit = sqlData.getShotsHit();
        shotsMissed = sqlData.getShotsMissed();
        displayName = sqlData.getDisplayname();
        playersKilled = sqlData.getPlayersKilled();
        friends = sqlData.getFriends();
    }


    protected void saveSQLData(Queues type, boolean won, int value) {
        PlayerUpdate update = new PlayerUpdate(sqlData);

        update.updateShotsMade(shotsHit);
        update.updateShotsMissed(shotsMissed);
        update.updatePlayersKilled(playersKilled);

        //update.push();

        sqlData = update.complete();
    }

    public Stream getStream() {
        return stream;
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
        if (this.queue != null) {
            queueTime = System.currentTimeMillis();
        } else {
            queueTime = 0;
        }
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
            client.getServer().onDisconnect(client);
        }
    }

    public void setInMatch(boolean isInMatch) {
        this.isInMatch = isInMatch;
    }

    public Rank getRanking() {
        return ranking;
    }

    public long getQueueJoinTime() {
        return queueTime;
    }

    public boolean isInsideQueueWindow(Player playerToCompare) {
        long waiting = (queue.getProcessStartTime() - queueTime) / 1000;

        int add = (int) (waiting / 2);

        int lowerBound = ranking.getRating() - add;
        int upperBound = ranking.getRating() + add;

        return playerToCompare.ranking.getRating() >= lowerBound && playerToCompare.ranking.getRating() <= upperBound;
    }

    @Override
    public int compareTo(Player o) {
        long waiting = (queue.getProcessStartTime() - queueTime) / 1000;

        int add = (int)(waiting / 2);
        int lowerBound = ranking.getRating() - add;

        long pWaiting = (queue.getProcessStartTime() - o.queueTime) / 1000;
        int pAdd = (int)(pWaiting / 2);
        int pLowerBound = o.ranking.getRating() - pAdd;

        return lowerBound - pLowerBound;
    }


    public void setPreferedServer(InetAddress preferedServer) {
        this.preferedServer = preferedServer;
    }

    public InetAddress getPreferedServer() {
        return preferedServer;
    }

    public void setName(String name) {
        this.username = name;
        this.sqlData.setUsername(name);
        this.sqlData.setDisplayName(name);
    }

    public void setCurrentAbility(Class<Ability<PlayableEntity>> class_) {
        try {
            this.ability = class_.newInstance(); //Has no owner
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("This ability is not compatible!");
        }
    }

    public Ability<PlayableEntity> getCurrentAbility() {
        return this.ability;
    }

    public void setPlayerID(int playerID) {
        this.sqlData.setId(playerID);
    }

    public void kick() throws IOException {
        kick("No reason specified");
    }

    public void kick(String reason) throws IOException {
        DisconnectReasonPacket packet = new DisconnectReasonPacket(client);
        packet.writePacket(reason);

        client.getServer().disconnect(client);
    }
}
