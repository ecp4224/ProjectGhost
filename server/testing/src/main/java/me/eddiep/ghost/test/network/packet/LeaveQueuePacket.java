package me.eddiep.ghost.test.network.packet;

import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.test.network.TcpUdpClient;
import me.eddiep.ghost.test.network.TcpUdpServer;
import me.eddiep.ghost.test.game.PlayerFactory;

import java.io.IOException;

public class LeaveQueuePacket extends Packet<TcpUdpServer, TcpUdpClient> {
    public LeaveQueuePacket(TcpUdpClient client) {
        super(client);
    }

    @Override
    public void onHandlePacket(TcpUdpClient client) throws IOException {
        byte type = consume(1).asByte();

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
        } else if (client.getPlayer().getQueue().queue().asByte() != type) {
            OkPacket packet = new OkPacket(client);
            packet.writePacket(false);
        } else {
            client.getPlayer().getQueue().removeUserFromQueue(client.getPlayer());
            OkPacket packet = new OkPacket(client);
            packet.writePacket(true);
        }
    }
}
