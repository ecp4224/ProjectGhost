package me.eddiep.ghost.central.network.packet.impl;

import me.eddiep.ghost.central.Main;
import me.eddiep.ghost.central.network.Client;
import me.eddiep.ghost.central.network.gameserv.GameServer;
import me.eddiep.ghost.central.network.gameserv.GameServerFactory;
import me.eddiep.ghost.central.network.packet.Packet;

import java.io.IOException;

public class QueueRequestPacket extends Packet {

    public QueueRequestPacket(Client client) {
        super(client);
    }

    @Override
    public void onHandlePacket(Client client)  throws IOException {
        byte toJoin = consume().asByte();

        GameServer serverToJoin = Main.gameServerFactory.findServerFor(toJoin);

        if (serverToJoin == null) {
            client.sendOk(false);
            return;
        }

        JoinServerPacket packet = new JoinServerPacket(client);
        packet.writePacket(serverToJoin);
    }
}
