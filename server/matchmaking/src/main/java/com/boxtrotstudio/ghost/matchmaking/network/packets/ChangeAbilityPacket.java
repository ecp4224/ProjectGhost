package com.boxtrotstudio.ghost.matchmaking.network.packets;

import com.boxtrotstudio.ghost.game.match.abilities.*;
import com.boxtrotstudio.ghost.matchmaking.network.PlayerClient;
import com.boxtrotstudio.ghost.matchmaking.network.TcpServer;
import com.boxtrotstudio.ghost.network.packet.Packet;
import com.boxtrotstudio.ghost.utils.Global;

import java.io.IOException;

public class ChangeAbilityPacket extends Packet<TcpServer, PlayerClient> {
    public static final Class[] WEAPONS = new Class[] {
            Gun.class,
            Laser.class,
            Circle.class,
            Dash.class,
            Boomerang.class
    };

    @Override
    public void onHandlePacket(PlayerClient client) throws IOException {
        byte action = consume(1).asByte();

        if (client.getPlayer() == null)
            return;


        if (action == 0x10) {
            client.getPlayer().setCurrentAbility(WEAPONS[Global.RANDOM.nextInt(WEAPONS.length)]);
        } else {
            client.getPlayer().setCurrentAbility(WEAPONS[action - 1]);
        }
    }
}
