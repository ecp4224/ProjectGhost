package com.boxtrotstudio.ghost.gameserver.api.network.packets;

import com.boxtrotstudio.ghost.gameserver.api.network.MatchmakingClient;
import com.boxtrotstudio.ghost.network.packet.Packet;
import com.boxtrotstudio.ghost.common.network.BaseServer;

import java.io.IOException;

public class GameServerAuthPacket extends Packet<BaseServer, MatchmakingClient> {
    public GameServerAuthPacket(MatchmakingClient client) {
        super(client);
    }

    @Override
    public void onWritePacket(MatchmakingClient client, Object... args) throws IOException {
        String secret = (String)args[0];
        long ID = (long)args[1];

        if (secret.length() != 32) {
            client.getServer().getLogger().error("Secret is not 32 characters!");
            System.exit(2);
            return;
        }

        write((byte)0x23)
                .write(secret)
                .write(ID)
                .endTCP();
    }
}
