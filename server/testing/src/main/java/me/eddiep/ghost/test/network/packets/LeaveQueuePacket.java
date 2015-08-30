package me.eddiep.ghost.test.network.packets;

import me.eddiep.ghost.common.game.PlayerFactory;
import me.eddiep.ghost.common.network.BasePlayerClient;
import me.eddiep.ghost.common.network.BaseServer;
import me.eddiep.ghost.common.network.packet.OkPacket;
import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.test.network.TestClient;

import java.io.IOException;

public class LeaveQueuePacket extends Packet<BaseServer, BasePlayerClient> {
    public LeaveQueuePacket(BasePlayerClient client) {
        super(client);
    }

    @Override
    public void onHandlePacket(BasePlayerClient c) throws IOException {
        TestClient client = (TestClient)c;

        byte type = consume(1).asByte();

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
        } else if (client.getTestPlayer().getQueue().queue().asByte() != type) {
            OkPacket packet = new OkPacket(client);
            packet.writePacket(false);
        } else {
            client.getTestPlayer().getQueue().removeUserFromQueue(client.getTestPlayer());
            OkPacket packet = new OkPacket(client);
            packet.writePacket(true);
        }
    }
}
