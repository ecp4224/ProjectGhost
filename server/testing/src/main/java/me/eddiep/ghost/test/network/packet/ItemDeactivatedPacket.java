package me.eddiep.ghost.test.network.packet;

import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.test.network.TcpUdpClient;
import me.eddiep.ghost.test.network.TcpUdpServer;

import java.io.IOException;

public class ItemDeactivatedPacket extends Packet<TcpUdpServer, TcpUdpClient> {
    public ItemDeactivatedPacket(TcpUdpClient client) {
        super(client);
    }

    @Override
    public void onWritePacket(TcpUdpClient client, Object... args) throws IOException {

        short type = (short) args[0];
        short id = (short)args[1];

        write((byte)0x33)
                .write(type)
                .write(id)
                .endTCP();
    }
}
