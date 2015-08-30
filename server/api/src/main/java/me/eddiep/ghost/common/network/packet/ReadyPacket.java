package me.eddiep.ghost.common.network.packet;

import me.eddiep.ghost.common.network.BasePlayerClient;
import me.eddiep.ghost.common.network.BaseServer;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class ReadyPacket extends Packet<BaseServer, BasePlayerClient> {

    public ReadyPacket(BasePlayerClient client) {
        super(client);
    }

    @Override
    public void onHandlePacket(BasePlayerClient client) throws IOException {
        client.getPlayer().setReady(consume().asBoolean());

        if (client.getPlayer().isReady()) {
            client.getPlayer().sendMatchMessage("Ready! Please wait for game to start.."); //TODO Change this message maybe..
        }
    }
}
