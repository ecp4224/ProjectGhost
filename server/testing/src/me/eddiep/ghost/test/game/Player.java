package me.eddiep.ghost.test.game;

import me.eddiep.ghost.game.match.LiveMatch;
import me.eddiep.ghost.game.match.Match;
import me.eddiep.ghost.game.match.entities.playable.impl.BaseNetworkPlayer;
import me.eddiep.ghost.network.notifications.Notification;
import me.eddiep.ghost.network.notifications.Request;
import me.eddiep.ghost.network.sql.PlayerData;
import me.eddiep.ghost.test.game.queue.PlayerQueue;
import me.eddiep.ghost.test.network.TcpUdpClient;
import me.eddiep.ghost.test.network.TcpUdpServer;
import me.eddiep.ghost.test.network.packet.DeleteRequestPacket;
import me.eddiep.ghost.test.network.packet.MatchStatusPacket;
import me.eddiep.ghost.test.network.packet.NewNotificationPacket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Player extends BaseNetworkPlayer<TcpUdpServer, TcpUdpClient> implements User {
    private PlayerQueue queue;
    private boolean isSpectating;

    protected Player(String username, String session, PlayerData sqlData) {
        super(username, session, sqlData);
    }

    /**
     * Create a new user with the provided username and SQL Data
     * @param username The username of this playable
     * @param sqlData The SQL data for associated with this playable
     * @return A new {@link Player} object
     */
    static Player createPlayer(String username, PlayerData sqlData) {
        UUID session;
        do {
            session = UUID.randomUUID();
        } while (PlayerFactory.findPlayerByUUID(session) != null);

        return new Player(username, session.toString(), sqlData);
    }

    @Override
    protected void onRemoveRequest(Request request) {
        if (client != null) {
            DeleteRequestPacket packet = new DeleteRequestPacket(client);
            try {
                packet.writePacket(request);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onSendNewNotification(Notification notification) {
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
     * Get a list of {@link me.eddiep.ghost.network.sql.PlayerData} objects of currently online friends
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
     * Set the queue this playable is currently waiting in <b>This method should only be called by {@link me.eddiep.ghost.test.game.queue.PlayerQueue} objects!</b>
     * @param queue The queue the playable joined
     */
    public void setQueue(PlayerQueue queue) {
        this.queue = queue;
        lastActive = System.currentTimeMillis();
    }

    @Override
    public void onWin(Match match) {

    }

    @Override
    public void onLose(Match match) {

    }

    public boolean isSpectating() {
        return isSpectating;
    }

    public void spectateMatch(LiveMatch match) {
        this.setQueue(null);
        this.setMatch(match);
        this.isSpectating = true;
    }

    public void sendMatchMessage(String message) {
        if (isInMatch() && !isSpectating) {
            MatchStatusPacket packet = new MatchStatusPacket(getClient());
            try {
                packet.writePacket(getMatch().isMatchActive(), message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
