package me.eddiep.ghost.matchmaking.packets;

import me.eddiep.ghost.matchmaking.TcpClient;
import me.eddiep.ghost.matchmaking.TcpServer;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class SetDisplayNamePacket extends Packet<TcpServer, TcpClient> {
    public SetDisplayNamePacket(TcpClient client) {
        super(client);
    }

    @Override
    public void onHandlePacket(TcpClient client)  throws IOException {
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
