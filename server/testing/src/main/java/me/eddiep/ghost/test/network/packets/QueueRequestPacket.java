package me.eddiep.ghost.test.network.packets;

import me.eddiep.ghost.common.game.PlayerFactory;
import me.eddiep.ghost.common.network.BasePlayerClient;
import me.eddiep.ghost.common.network.BaseServer;
import me.eddiep.ghost.common.network.packet.OkPacket;
import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.test.Main;
import me.eddiep.ghost.test.game.queue.PlayerQueue;
import me.eddiep.ghost.test.network.TestClient;

import java.io.IOException;

public class QueueRequestPacket extends Packet<BaseServer, BasePlayerClient> {

    public QueueRequestPacket(BasePlayerClient client) {
        super(client);
    }

    @Override
    public void onHandlePacket(BasePlayerClient c)  throws IOException {
        TestClient client = (TestClient)c;

        byte toJoin = consume().asByte();
        Queues type = Queues.byteToType(toJoin);

        if (!PlayerFactory.getCreator().checkSession(client.getPlayer().getSession())) {
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
            p.addUserToQueue(client.getTestPlayer());
            OkPacket packet = new OkPacket(client);
            packet.writePacket(true);
        }
    }
}
