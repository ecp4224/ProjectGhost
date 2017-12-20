package com.boxtrotstudio.ghost.matchmaking.network.packets;

import com.boxtrotstudio.ghost.matchmaking.core.hosts.gameserver.GameServer;
import com.boxtrotstudio.ghost.matchmaking.network.PlayerClient;
import com.boxtrotstudio.ghost.matchmaking.network.TcpServer;
import com.boxtrotstudio.ghost.network.packet.Packet;

import java.io.IOException;

public class MatchRedirectPacket extends Packet<TcpServer, PlayerClient> {
    public MatchRedirectPacket(PlayerClient client) {
        super(client);
    }

    @Override
    public void onWritePacket(PlayerClient client, Object... args) throws IOException {
        String ip;
        int port;

        if (args.length == 1) {
            GameServer server = (GameServer) args[0];

            ip = server.getIp();
            port = server.getPort();
        } else {
            ip = (String)args[0];
            port = (short)args[1];
        }

        write((byte)0x26)
                .write((byte)ip.length())
                .write(ip)
                .write(port)
                .endTCP();
    }
}
