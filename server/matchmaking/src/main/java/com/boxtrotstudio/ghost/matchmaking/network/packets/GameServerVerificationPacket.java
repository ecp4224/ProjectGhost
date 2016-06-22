package com.boxtrotstudio.ghost.matchmaking.network.packets;

import com.boxtrotstudio.ghost.matchmaking.network.GameServerClient;
import com.boxtrotstudio.ghost.matchmaking.network.TcpServer;
import com.boxtrotstudio.ghost.matchmaking.network.gameserver.GameServer;
import com.boxtrotstudio.ghost.matchmaking.network.gameserver.GameServerConfiguration;
import com.boxtrotstudio.ghost.matchmaking.network.gameserver.GameServerFactory;
import com.boxtrotstudio.ghost.network.packet.Packet;

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
            client.getServer().getLogger().debug("[SERVER] GameServer connection verified!");

            if (GameServerFactory.isConnected(ID)) {
                client.getServer().getLogger().debug("[SERVER] However, a GameServer with this ID is already connected...rejecting");
                client.disconnect();
                return;
            }

            GameServerConfiguration config = GameServerFactory.findServerConfig(ID);
            if (config == null) {
                client.getServer().getLogger().debug("[SERVER] However, this GameServer has no config file...rejecting");
                client.disconnect();
                return;
            }

            GameServer server = GameServerFactory.createFromConfig(client, config, ID);
            client.setGameServer(server);
            client.sendOk();

            GameServerStreamUpdatePacket packet = new GameServerStreamUpdatePacket(client);
            packet.writePacket(server.getConfig().getStream());

            //Main.SLACK_API.call(new SlackMessage("Gameserver #" + ID + " verified and connected."));
        } else {
            client.getServer().getLogger().error("[SERVER] Invalid secret sent! Disconnecting client!");
            client.disconnect();
        }
    }
}
