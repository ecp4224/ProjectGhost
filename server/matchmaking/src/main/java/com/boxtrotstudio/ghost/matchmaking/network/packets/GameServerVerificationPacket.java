package com.boxtrotstudio.ghost.matchmaking.network.packets;

import com.boxtrotstudio.ghost.matchmaking.network.GameServerClient;
import com.boxtrotstudio.ghost.matchmaking.network.TcpServer;
import com.boxtrotstudio.ghost.matchmaking.core.hosts.gameserver.GameServer;
import com.boxtrotstudio.ghost.matchmaking.core.hosts.gameserver.GameServerConfiguration;
import com.boxtrotstudio.ghost.matchmaking.core.hosts.gameserver.GameServerFactory;
import com.boxtrotstudio.ghost.network.packet.Packet;

import java.io.IOException;

public class GameServerVerificationPacket extends Packet<TcpServer, GameServerClient> {

    @Override
    public void onHandlePacket(GameServerClient client) throws IOException {
        String secret = consume(32).asString();

        long configID = consume(8).asLong();

        if (client.getServer().getConfig().getServerSecret().equals(secret)) {
            client.getServer().getLogger().debug("[SERVER] GameServer connection verified!");

            GameServerConfiguration config = GameServerFactory.findServerConfig(configID);
            if (config == null) {
                client.getServer().getLogger().debug("[SERVER] However, this GameServer has specified an non-existing config...rejecting");
                client.disconnect();
                return;
            }

            GameServer server = GameServerFactory.createFromConfig(client, config);
            client.setGameServer(server);
            client.sendOk();

            GameServerStreamUpdatePacket packet = new GameServerStreamUpdatePacket(client);
            packet.writePacket(server.getConfig().getStream());

            //Main.SLACK_API.call(new SlackMessage("Gameserver #" + configID + " verified and connected."));
        } else {
            client.getServer().getLogger().error("[SERVER] Invalid secret sent! Disconnecting client!");
            client.disconnect();
        }
    }
}
