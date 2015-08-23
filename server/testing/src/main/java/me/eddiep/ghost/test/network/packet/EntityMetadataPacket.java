package me.eddiep.ghost.test.network.packet;

import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.test.network.TcpUdpClient;
import me.eddiep.ghost.test.network.TcpUdpServer;

public class EntityMetadataPacket extends Packet<TcpUdpServer, TcpUdpClient> {
    public EntityMetadataPacket(TcpUdpClient client) {
        super(client);
    }

    @Override
    public void onWritePacket(TcpUdpClient client, Object... obj) {

    }
}
