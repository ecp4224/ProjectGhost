package me.eddiep.ghost.matchmaking.network.packets;

import me.eddiep.ghost.matchmaking.network.GameServerClient;
import me.eddiep.ghost.matchmaking.network.TcpServer;
import me.eddiep.ghost.matchmaking.network.gameserver.GameServer;
import me.eddiep.ghost.matchmaking.network.gameserver.GameServerConfiguration;
import me.eddiep.ghost.matchmaking.network.gameserver.GameServerFactory;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class GameServerVerificationPacket extends Packet<TcpServer, GameServerClient> {
    public GameServerVerificationPacket(GameServerClient client, byte[] data) {
        super(client, data);
    }

    @Override
    public void onHandlePacket(GameServerClient client) throws IOException {
        String secret = consume(32).asString();

        long ID = consume(8).asLong();

        if (client.getServer().getConfig().getServerSecret().equals(secret)) {
            System.out.println("[SERVER] GameServer connection verified!");

            if (GameServerFactory.isConnected(ID)) {
                System.out.println("[SERVER] However, a GameServer with this ID is already connected...rejecting");
                client.disconnect();
                return;
            }

            GameServerConfiguration config = GameServerFactory.findServerConfig(ID);
            if (config == null) {
                System.out.println("[SERVER] However, this GameServer has no config file...rejecting");
                client.disconnect();
                return;
            }

            GameServer server = GameServerFactory.createFromConfig(client, config, ID);
            client.setGameServer(server);
            client.sendOk();

            GameServerStreamUpdatePacket packet = new GameServerStreamUpdatePacket(client);
            packet.writePacket(server.getConfig().getStream());
        } else {
            System.err.println("[SERVER] Invalid secret sent! Disconnecting client!");
            client.disconnect();
        }
    }
}
