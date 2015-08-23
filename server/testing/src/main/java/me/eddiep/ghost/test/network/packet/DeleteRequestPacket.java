package me.eddiep.ghost.test.network.packet;

import me.eddiep.ghost.network.notifications.Request;
import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.test.network.TcpUdpClient;
import me.eddiep.ghost.test.network.TcpUdpServer;

import java.io.IOException;

public class DeleteRequestPacket extends Packet<TcpUdpServer, TcpUdpClient> {
    public DeleteRequestPacket(TcpUdpClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(TcpUdpClient client, Object... args) throws IOException {
        if (args.length != 1)
            return;

        Request request = (Request)args[0];

        write((byte)0x16)
                .write(request.getId())
                .endTCP();
    }
}
