package me.eddiep.ghost.test.network.packet;

import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.test.network.TcpUdpClient;
import me.eddiep.ghost.test.network.TcpUdpServer;

import java.io.IOException;

public class RespondRequestPacket extends Packet<TcpUdpServer, TcpUdpClient> {
    public RespondRequestPacket(TcpUdpClient client) {
        super(client);
    }

    @Override
    public void onHandlePacket(TcpUdpClient client)  throws IOException {
        int id = consume(4).asInt();
        boolean value = consume(1).asBoolean();

        if (client.getPlayer() != null) {
            client.getPlayer().respondToRequest(id, value);
        }
    }
}
