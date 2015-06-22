package me.eddiep.ghost.gameserver.packets;

import me.eddiep.ghost.gameserver.TcpUdpClient;
import me.eddiep.ghost.gameserver.TcpUdpServer;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class ReadyPacket extends Packet<TcpUdpServer, TcpUdpClient> {

    public ReadyPacket(TcpUdpClient client) {
        super(client);
    }

    @Override
    public void onHandlePacket(TcpUdpClient client) throws IOException {
        client.getPlayer().setReady(consume().asBoolean());
    }
}
