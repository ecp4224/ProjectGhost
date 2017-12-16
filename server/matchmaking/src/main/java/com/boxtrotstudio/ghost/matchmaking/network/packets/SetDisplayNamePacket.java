package com.boxtrotstudio.ghost.matchmaking.network.packets;

import com.boxtrotstudio.ghost.matchmaking.network.PlayerClient;
import com.boxtrotstudio.ghost.matchmaking.network.TcpServer;
import com.boxtrotstudio.ghost.network.packet.Packet;

import java.io.IOException;

@Deprecated
public class SetDisplayNamePacket extends Packet<TcpServer, PlayerClient> {
    public SetDisplayNamePacket(PlayerClient client) {
        super(client);
    }

    @Override
    public void onHandlePacket(PlayerClient client)  throws IOException {
        byte length = consume(1).asByte();
        String displayName = consume(length).asString();

        displayName = displayName.replaceAll("[^A-Za-z0-9 ]", "");

        //TODO Set displayname via login server
        /*if (client.getUser().getDisplayName().equals(displayName)) {
            OkPacket packet = new OkPacket(client);
            packet.writePacket(true);
        } else if (Main.SQL.displayNameExist(displayName)) {
            OkPacket packet = new OkPacket(client);
            packet.writePacket(false);
        } else {
            client.getUser().setDisplayName(displayName);
            OkPacket packet = new OkPacket(client);
            packet.writePacket(true);
        }*/
    }
}
