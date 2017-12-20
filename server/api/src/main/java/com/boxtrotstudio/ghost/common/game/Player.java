package com.boxtrotstudio.ghost.common.game;

import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.common.network.item.NetworkInventory;
import com.boxtrotstudio.ghost.common.network.packet.*;
import com.boxtrotstudio.ghost.common.network.world.NetworkWorld;
import com.boxtrotstudio.ghost.game.match.LiveMatch;
import com.boxtrotstudio.ghost.game.match.Match;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.entities.playable.impl.BaseNetworkPlayer;
import com.boxtrotstudio.ghost.game.match.item.Item;
import com.boxtrotstudio.ghost.game.match.stats.Stat;
import com.boxtrotstudio.ghost.game.team.Team;
import com.boxtrotstudio.ghost.network.notifications.Notification;
import com.boxtrotstudio.ghost.network.notifications.Request;
import com.boxtrotstudio.ghost.network.sql.PlayerData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Player extends BaseNetworkPlayer<BaseServer, BasePlayerClient> implements User {
    private boolean isSpectating;
    private boolean isGoingToSpectate;

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
        super.onItemActivated(item, owner);

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
     * Get a list of {@link PlayerData} objects of currently online friends
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

    public boolean hasStartedSpectating() {
        return isSpectating && !isGoingToSpectate;
    }

    public void spectateMatch(LiveMatch match) {
        this.setMatch(match);
        this.isSpectating = true;
        this.isGoingToSpectate = true;
    }

    public void spectateConnect() throws IOException {
        this.isGoingToSpectate = false;
        NetworkMatch match = (NetworkMatch)getMatch();

        match.addSpectator(this);
        Team team1 = match.getTeam1();
        Team team2 = match.getTeam2();

        MatchFoundPacket packet = new MatchFoundPacket(client);

        packet.writePacket(-1f, -1f, team1.getTeamMembers(), team2.getTeamMembers());

        match.spawnAllEntitiesFor(this);

        MapSettingsPacket packet3 = new MapSettingsPacket(client);
        packet3.writePacket(world.getWorldMap());


        MatchStatusPacket packet2 = new MatchStatusPacket(client);
        packet2.writePacket(match.isMatchActive(), match.getLastActiveReason());
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
