package com.boxtrotstudio.ghost.common.network.packet;

import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.network.packet.Packet;
import com.boxtrotstudio.ghost.common.network.BaseServer;

import java.io.IOException;

public class ReadyPacket extends Packet<BaseServer, BasePlayerClient> {

    public ReadyPacket(BasePlayerClient client) {
        super(client);
    }

    @Override
    public void onHandlePacket(BasePlayerClient client) throws IOException {
        client.getPlayer().setReady(consume().asBoolean());

        if (client.getPlayer().isReady()) {
            client.getPlayer().sendMatchMessage("Waiting for other players to connect..");
        }
    }
}
