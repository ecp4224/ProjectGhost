package com.boxtrotstudio.ghost.common.network.packet;

import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.network.packet.Packet;
import com.boxtrotstudio.ghost.common.network.BaseServer;

import java.io.IOException;

public class RespondRequestPacket extends Packet<BaseServer, BasePlayerClient> {
    public RespondRequestPacket(BasePlayerClient client) {
        super(client);
    }

    @Override
    public void onHandlePacket(BasePlayerClient client)  throws IOException {
        int id = consume(4).asInt();
        boolean value = consume(1).asBoolean();

        if (client.getPlayer() != null) {
            client.getPlayer().respondToRequest(id, value);
        }
    }
}
