package me.eddiep.ghost.matchmaking.network;

import me.eddiep.ghost.matchmaking.network.packets.PacketFactory;
import me.eddiep.ghost.matchmaking.player.Player;

import java.io.IOException;
import java.net.Socket;

public class PlayerClient extends TcpClient {
    private Player player;

    public PlayerClient(Player user, Socket socket, TcpServer server) throws IOException {
        super(socket, server);

        this.player = user;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public void handlePacket(byte opCode) throws IOException {
        PacketFactory.getPlayerPacket(opCode, PlayerClient.this).handlePacket().endTCP();
    }
}
