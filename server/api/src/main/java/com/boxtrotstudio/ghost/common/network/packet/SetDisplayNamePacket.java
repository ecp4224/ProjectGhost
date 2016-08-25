package com.boxtrotstudio.ghost.common.network.packet;

import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.network.packet.Packet;
import com.boxtrotstudio.ghost.utils.Global;

import java.io.IOException;

@Deprecated
public class SetDisplayNamePacket extends Packet<BaseServer, BasePlayerClient> {

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
