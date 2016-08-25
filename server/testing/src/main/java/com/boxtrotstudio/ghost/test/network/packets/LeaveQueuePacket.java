package com.boxtrotstudio.ghost.test.network.packets;

import com.boxtrotstudio.ghost.common.game.PlayerFactory;
import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.common.network.packet.OkPacket;
import com.boxtrotstudio.ghost.network.packet.Packet;
import com.boxtrotstudio.ghost.test.network.TestClient;

import java.io.IOException;

public class LeaveQueuePacket extends Packet<BaseServer, BasePlayerClient> {

    @Override
    public void onHandlePacket(BasePlayerClient c) throws IOException {
        TestClient client = (TestClient)c;

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

        if (client.getPlayer().isInMatch()) {
            OkPacket packet = new OkPacket(client);
            packet.writePacket(false);
        } else {
            client.getTestPlayer().getQueue().removeUserFromQueue(client.getTestPlayer());
            OkPacket packet = new OkPacket(client);
            packet.writePacket(true);
        }
    }
}
