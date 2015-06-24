package me.eddiep.ghost.matchmaking.network.packets;

import me.eddiep.ghost.matchmaking.network.PlayerClient;
import me.eddiep.ghost.matchmaking.network.TcpServer;
import me.eddiep.ghost.matchmaking.queue.Queues;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class QueueRequestPacket extends Packet<TcpServer, PlayerClient> {

    public QueueRequestPacket(PlayerClient client) {
        super(client);
    }

    @Override
    public void onHandlePacket(PlayerClient client)  throws IOException {
        byte toJoin = consume().asByte();
        Queues type = Queues.byteToType(toJoin);

        //TODO Check session via login server
        /*if (!PlayerFactory.checkSession(client.getUser().getSession().toString())) {
            OkPacket packet = new OkPacket(client);
            packet.writePacket(false);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            client.getServer().disconnect(client);
            return;
        }*/

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
