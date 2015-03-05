package me.eddiep.ghost.server.network.packet.impl;

import me.eddiep.ghost.server.game.Player;
import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.network.packet.Packet;

import java.io.IOException;

public class ClientStatePacket extends Packet {
    public ClientStatePacket(Client client) {
        super(client);
    }

    @Override
    protected void onWritePacket(Client client, Object... args) throws IOException {
        if (args.length != 1)
            return;

        Player player = (Player)args[0];
        boolean isVisible = player.equals(client.getPlayer()) || player.isVisible();
        int lastWrite = client.getLastWritePacket() + 1;
        client.setLastWritePacket(lastWrite);

        client.getServer().sendUdpPacket(
                write((byte)0x04)
                .write(lastWrite)
                .write(player.equals(client.getPlayer()))
                .write(player.getPosition().x)
                .write(player.getPosition().y)
                .write(player.getVelocity().x)
                .write(player.getVelocity().y)
                .write(isVisible)
                .write(player.getMatch().getTimeElapsed())
                .endUDP()
        );
    }
}
