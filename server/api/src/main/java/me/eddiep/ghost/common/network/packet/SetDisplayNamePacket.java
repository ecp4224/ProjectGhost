package me.eddiep.ghost.common.network.packet;

import me.eddiep.ghost.common.network.BasePlayerClient;
import me.eddiep.ghost.common.network.BaseServer;
import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.utils.Global;

import java.io.IOException;

public class SetDisplayNamePacket extends Packet<BaseServer, BasePlayerClient> {
    public SetDisplayNamePacket(BasePlayerClient client) {
        super(client);
    }

    @Override
    public void onHandlePacket(BasePlayerClient client)  throws IOException {
        byte length = consume(1).asByte();
        String displayName = consume(length).asString();

        displayName = displayName.replaceAll("[^A-Za-z0-9 ]", "");

        if (client.getPlayer().getDisplayName().equals(displayName)) {
            OkPacket packet = new OkPacket(client);
            packet.writePacket(true);
        } else if (Global.SQL.displayNameExist(displayName)) {
            OkPacket packet = new OkPacket(client);
            packet.writePacket(false);
        } else {
            client.getPlayer().setDisplayName(displayName);
            OkPacket packet = new OkPacket(client);
            packet.writePacket(true);
        }
    }
}
