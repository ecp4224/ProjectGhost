package me.eddiep.ghost.matchmaking.packets;

import me.eddiep.ghost.matchmaking.TcpClient;
import me.eddiep.ghost.matchmaking.TcpServer;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class RespondRequestPacket extends Packet<TcpServer, TcpClient> {
    public RespondRequestPacket(TcpClient client) {
        super(client);
    }

    @Override
    public void onHandlePacket(TcpClient client)  throws IOException {
        int id = consume(4).asInt();
        boolean value = consume(1).asBoolean();

        if (client.getUser() != null) {
            client.getUser().respondToRequest(id, value);
        }
    }
}
