package me.eddiep.ghost.test.network.packet;

import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.test.Main;
import me.eddiep.ghost.test.network.TcpUdpClient;
import me.eddiep.ghost.test.network.TcpUdpServer;
import me.eddiep.ghost.test.game.PlayerFactory;
import me.eddiep.ghost.test.game.queue.PlayerQueue;

import java.io.IOException;

public class QueueRequestPacket extends Packet<TcpUdpServer, TcpUdpClient> {

    public QueueRequestPacket(TcpUdpClient client) {
        super(client);
    }

    @Override
    public void onHandlePacket(TcpUdpClient client)  throws IOException {
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

        PlayerQueue p = Main.playerQueueHashMap.get(type);

        if (client.getPlayer().isInMatch()) {
            OkPacket packet = new OkPacket(client);
            packet.writePacket(false);
        }
        else if (p == null) {
            OkPacket packet = new OkPacket(client);
            packet.writePacket(false);
        } else {
            p.addUserToQueue(client.getPlayer());
            OkPacket packet = new OkPacket(client);
            packet.writePacket(true);
        }
    }
}
