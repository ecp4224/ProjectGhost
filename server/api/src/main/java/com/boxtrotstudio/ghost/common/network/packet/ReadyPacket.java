package com.boxtrotstudio.ghost.common.network.packet;

import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.network.packet.Packet;

import java.io.IOException;

public class ReadyPacket extends Packet<BaseServer, BasePlayerClient> {

    @Override
    public void onHandlePacket(BasePlayerClient client) throws IOException {
        client.getPlayer().setReady(consume().asBoolean());

        if (client.getPlayer().isReady()) {
            client.getPlayer().sendMatchMessage(client.getPlayer().getMatch().getLastActiveReason());
        }

        client.sendOk();
    }
}
