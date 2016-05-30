package com.boxtrotstudio.ghost.matchmaking.network.packets;

import com.boxtrotstudio.ghost.matchmaking.network.PlayerClient;
import com.boxtrotstudio.ghost.matchmaking.network.TcpServer;
import com.boxtrotstudio.ghost.network.packet.Packet;

import java.io.IOException;

public class RespondRequestPacket extends Packet<TcpServer, PlayerClient> {
    public RespondRequestPacket(PlayerClient client, byte[] data) {
        super(client, data);
    }

    @Override
    public void onHandlePacket(PlayerClient client)  throws IOException {
        int id = consume(4).asInt();
        boolean value = consume(1).asBoolean();

        if (client.getPlayer() != null) {
            client.getPlayer().respondToRequest(id, value);
        }
    }
}
