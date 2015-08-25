package me.eddiep.ghost.gameserver.api.network.packets;

import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.gameserver.api.network.TcpUdpClient;
import me.eddiep.ghost.gameserver.api.network.TcpUdpServer;

import java.io.IOException;

public class ItemDeactivatedPacket extends Packet<TcpUdpServer, TcpUdpClient> {
    public ItemDeactivatedPacket(TcpUdpClient client) {
        super(client);
    }

    @Override
    public void onWritePacket(TcpUdpClient client, Object... args) throws IOException {

        short type = (short) args[0];

        write((byte)0x33)
                .write(type)
                .endTCP();
    }
}
