package me.eddiep.ghost.central.network.gameserv;

import me.eddiep.ghost.central.TcpServer;
import me.eddiep.ghost.central.network.Client;
import me.eddiep.ghost.central.network.dataserv.PlayerData;

import java.io.IOException;
import java.net.Socket;

public class GameServerConnection extends Client {
    private GameServer owner;

    public GameServerConnection(Socket socket, TcpServer server, GameServer owner) throws IOException {
        super(socket, server, owner.getSecret(), null);
    }

    public GameServer getGameServer() {
        return owner;
    }
}
