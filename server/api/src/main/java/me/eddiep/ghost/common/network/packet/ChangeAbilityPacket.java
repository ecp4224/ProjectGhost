package me.eddiep.ghost.common.network.packet;

import me.eddiep.ghost.common.network.BasePlayerClient;
import me.eddiep.ghost.common.network.BaseServer;
import me.eddiep.ghost.game.match.abilities.*;
import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.utils.Global;

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
