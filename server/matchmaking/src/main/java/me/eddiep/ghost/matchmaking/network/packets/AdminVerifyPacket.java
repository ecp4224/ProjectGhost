package me.eddiep.ghost.matchmaking.network.packets;

import me.eddiep.ghost.matchmaking.network.AdminClient;
import me.eddiep.ghost.matchmaking.network.TcpServer;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class AdminVerifyPacket extends Packet<TcpServer, AdminClient> {
    public AdminVerifyPacket(AdminClient client) {
        super(client);
    }

    @Override
    public void onHandlePacket(AdminClient client) throws IOException {
        int size = consume(4).asInt();
        String secret = consume(size).asString();

        if (client.getServer().getConfig().getAdminSecret().equals(secret)) {
            System.err.println("[ADMIN] Admin connected from " + client.getIpAddress());
        } else {
            System.err.println("[ADMIN] Admin failed to connect from " + client.getIpAddress());
            client.disconnect();
        }
    }
}
