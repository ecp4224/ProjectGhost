package me.eddiep.ghost.server.network.dataserv;

import me.eddiep.ghost.server.TcpUdpServer;
import me.eddiep.ghost.server.game.entities.Player;
import me.eddiep.ghost.server.network.Client;

import java.io.IOException;
import java.net.Socket;

public class CentralServer extends Client {
    public CentralServer(Socket socket, TcpUdpServer server) throws IOException {
        super(null, socket, server);
    }

    public boolean addPlayerToQueue(String uuid) {
        //TODO Add player to queue
        //TODO Or wait for player to connect then add to queue

        return false;
    }

    public boolean removePlayerFromQueue(String uuid) {
        //TODO Remove player from queue

        return false;
    }

    public void onPlayerConnected() {

    }
}
