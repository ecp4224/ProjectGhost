package com.boxtrotstudio.ghost.matchmaking.network.packets;

import com.boxtrotstudio.ghost.matchmaking.network.PlayerClient;
import com.boxtrotstudio.ghost.matchmaking.network.TcpServer;
import com.boxtrotstudio.ghost.network.packet.Packet;

import java.io.IOException;

public class LeaveQueuePacket extends Packet<TcpServer, PlayerClient> {
    public LeaveQueuePacket(PlayerClient client, byte[] data) {
        super(client, data);
    }

    @Override
    public void onHandlePacket(PlayerClient client) throws IOException {
        byte type = consume(1).asByte();

        //TODO Check session via login server
        /*if (!PlayerFactory.checkSession(client.getUser().getSession().toString())) {
            OkPacket packet = new OkPacket(client);
            packet.writePacket(false);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            client.getServer().onDisconnect(client);
            return;
        }*/

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
