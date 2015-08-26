package me.eddiep.ghost.gameserver.api.network.packets;

import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.gameserver.api.network.TcpUdpClient;
import me.eddiep.ghost.gameserver.api.network.TcpUdpServer;

import java.io.IOException;

public class ItemActivatedPacket extends Packet<TcpUdpServer, TcpUdpClient> {
    public ItemActivatedPacket(TcpUdpClient client) {
        super(client);
    }

    @Override
    public void onWritePacket(TcpUdpClient client, Object... args) throws IOException {

        short type = (short) args[0];

        write((byte)0x32)
                .write(type)
                .endTCP();
    }
}
