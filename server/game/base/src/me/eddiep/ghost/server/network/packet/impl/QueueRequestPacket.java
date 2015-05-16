package me.eddiep.ghost.server.network.packet.impl;

import me.eddiep.ghost.server.Starter;
import me.eddiep.ghost.server.game.entities.PlayerFactory;
import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.network.dataserv.CentralServer;
import me.eddiep.ghost.server.network.packet.Packet;

import java.io.IOException;

public class QueueRequestPacket extends Packet {

    public QueueRequestPacket(Client client) {
        super(client);
    }

    @Override
    public void onHandlePacket(Client client)  throws IOException {
        if (client instanceof CentralServer) {
            int uuidSize = consume(4).asInt();
            String uuid = consume(uuidSize).asString();

            boolean result = ((CentralServer)client).addPlayerToQueue(uuid);

            OkPacket packet = new OkPacket(client);
            packet.writePacket(result);

        } else {
            client.disconnect();
        }
    }
}
