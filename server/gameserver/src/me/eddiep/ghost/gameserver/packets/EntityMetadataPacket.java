package me.eddiep.ghost.gameserver.packets;


import me.eddiep.ghost.gameserver.TcpUdpClient;
import me.eddiep.ghost.gameserver.TcpUdpServer;
import me.eddiep.ghost.network.packet.Packet;

public class EntityMetadataPacket extends Packet<TcpUdpServer, TcpUdpClient> {
    public EntityMetadataPacket(TcpUdpClient client) {
        super(client);
    }

    @Override
    public void onWritePacket(TcpUdpClient client, Object... obj) {

    }
}
