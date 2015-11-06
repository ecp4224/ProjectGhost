package me.eddiep.ghost.matchmaking.network.packets;

import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.matchmaking.Main;
import me.eddiep.ghost.matchmaking.network.PlayerClient;
import me.eddiep.ghost.matchmaking.network.TcpServer;
import me.eddiep.ghost.matchmaking.player.PlayerFactory;
import me.eddiep.ghost.matchmaking.queue.PlayerQueue;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class QueueRequestPacket extends Packet<TcpServer, PlayerClient> {

    public QueueRequestPacket(PlayerClient client, byte[] data) {
        super(client, data);
    }

    @Override
    public void onHandlePacket(PlayerClient client)  throws IOException {
        byte toJoin = consume().asByte();
        Queues type = Queues.byteToType(toJoin);
        PlayerQueue queue = Main.getQueueFor(type, client.getPlayer().getStream());

        if (!PlayerFactory.checkSession(client.getPlayer().getSession())) {
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
        else if (queue == null) {
            OkPacket packet = new OkPacket(client);
            packet.writePacket(false);
        } else {
            queue.addUserToQueue(client.getPlayer());
            OkPacket packet = new OkPacket(client);
            packet.writePacket(true);
        }
    }
}
