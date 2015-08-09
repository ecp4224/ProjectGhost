package me.eddiep.ghost.test.network.packet;

import me.eddiep.ghost.game.match.abilities.Circle;
import me.eddiep.ghost.game.match.abilities.Dash;
import me.eddiep.ghost.game.match.abilities.Gun;
import me.eddiep.ghost.game.match.abilities.Laser;
import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.test.network.TcpUdpClient;
import me.eddiep.ghost.test.network.TcpUdpServer;

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

            case 4:
                client.getPlayer().setCurrentAbility(Dash.class);
                break;
        }
    }
}
