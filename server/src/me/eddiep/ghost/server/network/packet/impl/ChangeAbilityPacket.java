package me.eddiep.ghost.server.network.packet.impl;

import me.eddiep.ghost.server.game.entities.abilities.Circle;
import me.eddiep.ghost.server.game.entities.abilities.Gun;
import me.eddiep.ghost.server.game.entities.abilities.Laser;
import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.network.packet.Packet;

import java.io.IOException;

public class ChangeAbilityPacket extends Packet {
    public ChangeAbilityPacket(Client client) {
        super(client);
    }

    @Override
    public void onHandlePacket(Client client) throws IOException {
        byte action = consume(1).asByte();

        if (client.getPlayer() == null)
            return;

        switch (action) {
            case 1:
                client.getPlayer().setCurrentAbility(Gun.class);
                break;

            case 2:
                client.getPlayer().setCurrentAbility(Laser.class);
                break;

            case 3:
                client.getPlayer().setCurrentAbility(Circle.class);
                break;
        }
    }
}
