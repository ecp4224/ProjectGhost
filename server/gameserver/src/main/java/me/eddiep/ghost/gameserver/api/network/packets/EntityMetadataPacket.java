package me.eddiep.ghost.gameserver.api.network.packets;


import me.eddiep.ghost.gameserver.api.network.TcpUdpClient;
import me.eddiep.ghost.gameserver.api.network.TcpUdpServer;
import me.eddiep.ghost.network.packet.Packet;

public class EntityMetadataPacket extends Packet<TcpUdpServer, TcpUdpClient> {
    public EntityMetadataPacket(TcpUdpClient client) {
        super(client);
    }

    @Override
    public void onWritePacket(TcpUdpClient client, Object... obj) {

    }
}
