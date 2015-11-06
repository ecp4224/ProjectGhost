package me.eddiep.ghost.common.game;

import me.eddiep.ghost.common.network.BasePlayerClient;
import me.eddiep.ghost.common.network.BaseServer;
import me.eddiep.ghost.common.network.item.NetworkInventory;
import me.eddiep.ghost.common.network.packet.*;
import me.eddiep.ghost.common.network.world.NetworkWorld;
import me.eddiep.ghost.game.match.LiveMatch;
import me.eddiep.ghost.game.match.Match;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.entities.playable.impl.BaseNetworkPlayer;
import me.eddiep.ghost.game.match.item.Item;
import me.eddiep.ghost.game.match.stats.Stat;
import me.eddiep.ghost.network.notifications.Notification;
import me.eddiep.ghost.network.notifications.Request;
import me.eddiep.ghost.network.sql.PlayerData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Player extends BaseNetworkPlayer<BaseServer, BasePlayerClient> implements User {
    private boolean isSpectating;

    public Player(String username, String session, PlayerData sqlData) {
        super(username, session, sqlData);

        super.inventory = new NetworkInventory(2, this);
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

    @Override
    public void onItemActivated(Item item, PlayableEntity owner) {
        try {
            short id = owner.getID();
            if (id == getID())
                id = 0; //This player is the owner

            new ItemActivatedPacket(client).writePacket(item.getEntity().getType(), id);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemDeactivated(Item item, PlayableEntity owner) {
        try {
            short id = owner.getID();
            if (id == getID())
                id = 0; //This player is the owner

            new ItemDeactivatedPacket(client).writePacket(item.getEntity().getType(), id);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get a list of {@link Player} objects of currently online friends
     * @return A {@link java.util.List} of currently online friends
     */
    public List<Player> getOnlineFriends() {
        ArrayList<Player> toReturn = new ArrayList<>();
        for (long l : friends) {
            Player p = PlayerFactory.getCreator().findPlayerById(l);
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
            Player p = PlayerFactory.getCreator().findPlayerById(l);
            if (p != null)
                toReturn.add(p.getStats());
        }

        return toReturn;
    }

    @Override
    public void onWin(Match match) {

    }

    @Override
    public void onLose(Match match) {

    }

    @Override
    public void onStatUpdate(Stat stat) {
        super.onStatUpdate(stat);
        try {
            new StatUpdatePacket(getClient()).writePacket(stat);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isSpectating() {
        return isSpectating;
    }

    public void spectateMatch(LiveMatch match) {
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

    public void stopSpectating() {
        ((NetworkWorld)this.getMatch().getWorld()).removeSpectator(this);
        this.setMatch(null);
        this.isSpectating = false;
    }
}
