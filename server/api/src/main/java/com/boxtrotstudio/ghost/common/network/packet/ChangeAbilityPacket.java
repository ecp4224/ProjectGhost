package com.boxtrotstudio.ghost.common.network.packet;

import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.game.match.abilities.*;
import com.boxtrotstudio.ghost.network.packet.Packet;
import com.boxtrotstudio.ghost.utils.Global;

import java.io.IOException;

public class ChangeAbilityPacket extends Packet<BaseServer, BasePlayerClient> {
    public static final Class[] WEAPONS = new Class[] {
            Gun.class,
            Laser.class,
            Circle.class,
            Dash.class,
            Boomerang.class
    };

    public ChangeAbilityPacket(BasePlayerClient client) {
        super(client);
    }

    @Override
    public void onHandlePacket(BasePlayerClient client) throws IOException {
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
