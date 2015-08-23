package me.eddiep.ghost.matchmaking.network.packets;

import main.java.matchmaking.network.PlayerClient;
import main.java.matchmaking.network.TcpServer;
import main.java.matchmaking.network.gameserver.GameServer;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class MatchRedirectPacket extends Packet<TcpServer, PlayerClient> {
    public MatchRedirectPacket(PlayerClient client) {
        super(client);
    }

    @Override
    public void onWritePacket(PlayerClient client, Object... args) throws IOException {
        GameServer server = (GameServer)args[0];

        String ip = server.getConfig().getIp();
        short port = server.getConfig().getPort();

        write((byte)0x26)
                .write((byte)ip.length())
                .write(ip)
                .write(port)
                .endTCP();
    }
}
