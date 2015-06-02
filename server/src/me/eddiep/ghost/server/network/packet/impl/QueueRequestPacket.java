package me.eddiep.ghost.server.network.packet.impl;

import me.eddiep.ghost.server.game.entities.playable.impl.PlayerFactory;
import me.eddiep.ghost.server.game.queue.Queues;
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
        Queues type = Queues.byteToType(toJoin);

        if (!PlayerFactory.checkSession(client.getPlayer().getSession().toString())) {
            OkPacket packet = new OkPacket(client);
            packet.writePacket(false);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            client.getServer().disconnect(client);
            return;
        }

        if (client.getPlayer().isInMatch()) {
            OkPacket packet = new OkPacket(client);
            packet.writePacket(false);
        }
        else if (type.getQueue() == null) {
            OkPacket packet = new OkPacket(client);
            packet.writePacket(false);
        } else {
            type.getQueue().addUserToQueue(client.getPlayer());
            OkPacket packet = new OkPacket(client);
            packet.writePacket(true);
        }
    }
}
