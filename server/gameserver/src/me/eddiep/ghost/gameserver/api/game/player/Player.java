package me.eddiep.ghost.gameserver.api.game.player;

import me.eddiep.ghost.game.Match;
import me.eddiep.ghost.game.entities.playable.impl.BaseNetworkPlayer;
import me.eddiep.ghost.gameserver.api.network.TcpUdpClient;
import me.eddiep.ghost.gameserver.api.network.TcpUdpServer;
import me.eddiep.ghost.gameserver.api.network.User;
import me.eddiep.ghost.network.sql.PlayerData;

public class Player extends BaseNetworkPlayer<TcpUdpServer, TcpUdpClient> implements User {
    protected Player(String username, String session, PlayerData sqlData) {
        super(username, session, sqlData);
    }

    @Override
    public void onWin(Match match) {

    }

    @Override
    public void onLose(Match match) {

    }
}
