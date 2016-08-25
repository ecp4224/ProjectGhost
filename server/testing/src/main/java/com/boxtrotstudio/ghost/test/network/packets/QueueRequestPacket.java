package com.boxtrotstudio.ghost.test.network.packets;

import com.boxtrotstudio.ghost.common.game.PlayerFactory;
import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.common.network.packet.OkPacket;
import com.boxtrotstudio.ghost.network.packet.Packet;
import com.boxtrotstudio.ghost.test.game.queue.PlayerQueue;
import com.boxtrotstudio.ghost.test.network.TestClient;
import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.game.queue.Queues;
import com.boxtrotstudio.ghost.test.Main;

import java.io.IOException;

public class QueueRequestPacket extends Packet<BaseServer, BasePlayerClient> {

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
