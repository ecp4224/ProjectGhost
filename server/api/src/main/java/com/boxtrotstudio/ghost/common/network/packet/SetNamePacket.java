package com.boxtrotstudio.ghost.common.network.packet;

import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.network.packet.Packet;
import com.boxtrotstudio.ghost.utils.Global;

import java.io.IOException;

public class SetNamePacket extends Packet<BaseServer, BasePlayerClient> {

    @Override
    public void onHandlePacket(BasePlayerClient client) throws IOException {
        String name = consume(255).asString();

        if (Global.isOffline())
            return;

        client.getPlayer().setName(name.trim());
    }
}
