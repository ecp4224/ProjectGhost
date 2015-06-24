package me.eddiep.ghost.gameserver.api.network.packets;

import me.eddiep.ghost.gameserver.api.network.TcpUdpServer;
import me.eddiep.ghost.network.Client;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class MatchEndPacket extends Packet<TcpUdpServer, Client<TcpUdpServer>> {
    public MatchEndPacket(Client<TcpUdpServer> client) {
        super(client);
    }

    @Override
    protected void onWritePacket(Client<TcpUdpServer> client, Object... args) throws IOException {
        if (args.length != 2)
            return;

        Boolean winrar = (Boolean) args[0];
        Long matchId = (Long)args[1];

        write((byte)0x07)
                .write(winrar)
                .write(matchId)
                .endTCP();
    }
}
