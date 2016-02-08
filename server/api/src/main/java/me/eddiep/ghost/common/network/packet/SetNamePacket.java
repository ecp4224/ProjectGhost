package me.eddiep.ghost.common.network.packet;

import me.eddiep.ghost.common.network.BasePlayerClient;
import me.eddiep.ghost.common.network.BaseServer;
import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.utils.Global;

import java.io.IOException;

public class SetNamePacket extends Packet<BaseServer, BasePlayerClient> {
    public SetNamePacket(BasePlayerClient client) {
        super(client);
    }

    @Override
    public void onHandlePacket(BasePlayerClient client) throws IOException {
        String name = consume(255).asString();

        if (Global.isOffline())
            return;

        client.getPlayer().setName(name.trim());
    }
}
