package me.eddiep.ghost.gameserver.api.game.player;

import me.eddiep.ghost.game.match.Match;
import me.eddiep.ghost.game.match.entities.playable.impl.BaseNetworkPlayer;
import me.eddiep.ghost.game.match.stats.Stat;
import me.eddiep.ghost.gameserver.api.network.TcpUdpClient;
import me.eddiep.ghost.gameserver.api.network.TcpUdpServer;
import me.eddiep.ghost.gameserver.api.network.User;
import me.eddiep.ghost.gameserver.api.network.packets.MatchStatusPacket;
import me.eddiep.ghost.gameserver.api.network.packets.StatUpdatePacket;
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
    public void onStatUpdate(Stat stat) {
        try {
            new StatUpdatePacket(getClient()).writePacket(stat);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
