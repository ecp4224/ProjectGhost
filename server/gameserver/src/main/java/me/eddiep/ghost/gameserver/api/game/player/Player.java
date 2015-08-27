package me.eddiep.ghost.gameserver.api.game.player;

import me.eddiep.ghost.game.match.LiveMatch;
import me.eddiep.ghost.game.match.Match;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.entities.playable.impl.BaseNetworkPlayer;
import me.eddiep.ghost.game.match.item.Item;
import me.eddiep.ghost.game.match.stats.Stat;
import me.eddiep.ghost.gameserver.api.network.TcpUdpClient;
import me.eddiep.ghost.gameserver.api.network.TcpUdpServer;
import me.eddiep.ghost.gameserver.api.network.User;
import me.eddiep.ghost.gameserver.api.network.packets.ItemActivatedPacket;
import me.eddiep.ghost.gameserver.api.network.packets.ItemDeactivatedPacket;
import me.eddiep.ghost.gameserver.api.network.packets.MatchStatusPacket;
import me.eddiep.ghost.gameserver.api.network.packets.StatUpdatePacket;
import me.eddiep.ghost.gameserver.api.network.world.NetworkWorld;
import me.eddiep.ghost.network.sql.PlayerData;

import java.io.IOException;

public class Player extends BaseNetworkPlayer<TcpUdpServer, TcpUdpClient> implements User {
    private boolean isSpectating;

    protected Player(String username, String session, PlayerData sqlData) {
        super(username, session, sqlData);
    }

    @Override
    public void onWin(Match match) {

    }

    @Override
    public void onLose(Match match) {

    }

    @Override
    public void onItemActivated(Item item, PlayableEntity owner) {
        try {
            new ItemActivatedPacket(client).writePacket(item.getEntity().getType());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemDeactivated(Item item, PlayableEntity owner) {
        try {
            new ItemDeactivatedPacket(client).writePacket(item.getEntity().getType());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStatUpdate(Stat stat) {
        try {
            new StatUpdatePacket(getClient()).writePacket(stat);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public boolean isSpectating() {
        return isSpectating;
    }

    public void stopSpectating() {
        ((NetworkWorld)this.getMatch().getWorld()).removeSpectator(this);
        this.setMatch(null);
        this.isSpectating = false;
    }
}
