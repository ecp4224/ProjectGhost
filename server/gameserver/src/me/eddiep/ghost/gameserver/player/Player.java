package me.eddiep.ghost.gameserver.player;

import me.eddiep.ghost.gameserver.TcpUdpClient;
import me.eddiep.ghost.gameserver.TcpUdpServer;
import me.eddiep.ghost.network.sql.PlayerData;
import me.eddiep.ghost.game.entities.playable.impl.BaseNetworkPlayer;

import java.util.UUID;

public class Player extends BaseNetworkPlayer<TcpUdpServer, TcpUdpClient> {
    protected Player(String username, UUID session, PlayerData sqlData) {
        super(username, session, sqlData);
    }
}
