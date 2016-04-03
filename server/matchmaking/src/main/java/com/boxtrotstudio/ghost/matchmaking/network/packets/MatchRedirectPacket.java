package com.boxtrotstudio.ghost.matchmaking.network.packets;

import com.boxtrotstudio.ghost.matchmaking.network.PlayerClient;
import com.boxtrotstudio.ghost.matchmaking.network.gameserver.GameServer;
import com.boxtrotstudio.ghost.network.packet.Packet;
import com.boxtrotstudio.ghost.matchmaking.network.TcpServer;

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
