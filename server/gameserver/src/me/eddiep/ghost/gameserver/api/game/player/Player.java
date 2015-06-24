package me.eddiep.ghost.gameserver.api.game.player;

import me.eddiep.ghost.gameserver.api.network.TcpUdpClient;
import me.eddiep.ghost.gameserver.api.network.TcpUdpServer;
import me.eddiep.ghost.gameserver.api.network.User;
import me.eddiep.ghost.network.sql.PlayerData;
import me.eddiep.ghost.game.entities.playable.impl.BaseNetworkPlayer;

import java.util.UUID;

public class Player extends BaseNetworkPlayer<TcpUdpServer, TcpUdpClient> implements User {
    protected Player(String username, UUID session, PlayerData sqlData) {
        super(username, session, sqlData);
    }
}
