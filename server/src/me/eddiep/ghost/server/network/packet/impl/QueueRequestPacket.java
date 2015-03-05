package me.eddiep.ghost.server.network.packet.impl;

import me.eddiep.ghost.server.game.queue.QueueType;
import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.network.packet.Packet;

import java.io.IOException;

public class QueueRequestPacket extends Packet {

    public QueueRequestPacket(Client client) {
        super(client);
    }

    @Override
    public void onHandlePacket(Client client)  throws IOException {
        byte toJoin = consume().asByte();
        QueueType type = QueueType.byteToType(toJoin);

        if (type.getQueue() == null) {
            OkPacket packet = new OkPacket(client);
            packet.writePacket(false);
        } else {
            type.getQueue().addUserToQueue(client.getPlayer());
            OkPacket packet = new OkPacket(client);
            packet.writePacket(true);
        }
    }
}
