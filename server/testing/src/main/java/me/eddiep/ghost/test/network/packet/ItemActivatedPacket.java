package me.eddiep.ghost.test.network.packet;

import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.test.network.TcpUdpClient;
import me.eddiep.ghost.test.network.TcpUdpServer;

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
