package me.eddiep.ghost.gameserver.packets;

import me.eddiep.ghost.gameserver.TcpUdpClient;
import me.eddiep.ghost.gameserver.TcpUdpServer;
import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.game.entities.abilities.Circle;
import me.eddiep.ghost.game.entities.abilities.Gun;
import me.eddiep.ghost.game.entities.abilities.Laser;

import java.io.IOException;

public class ChangeAbilityPacket extends Packet<TcpUdpServer, TcpUdpClient> {
    public ChangeAbilityPacket(TcpUdpClient client) {
        super(client);
    }

    @Override
    public void onHandlePacket(TcpUdpClient client) throws IOException {
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