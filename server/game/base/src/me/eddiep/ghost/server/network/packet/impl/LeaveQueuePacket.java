package me.eddiep.ghost.server.network.packet.impl;

import me.eddiep.ghost.server.Starter;
import me.eddiep.ghost.server.game.entities.Player;
import me.eddiep.ghost.server.game.entities.PlayerFactory;
import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.network.dataserv.CentralServer;
import me.eddiep.ghost.server.network.packet.Packet;

import java.io.IOException;

public class LeaveQueuePacket extends Packet {
    public LeaveQueuePacket(Client client) {
        super(client);
    }

    @Override
    public void onHandlePacket(Client client) throws IOException {
        byte toLeave = consume().asByte();

        Player p = client.getPlayer();
        if (p == null)
            return;

        Starter.getGame().playerQueueProcessor().removeUserFromQueue(p);
        client.disconnect();
    }
}
