package me.eddiep.ghost.gameserver.api.network.packets;

import me.eddiep.ghost.common.network.BaseServer;
import me.eddiep.ghost.gameserver.api.network.MatchmakingClient;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class GameServerAuthPacket extends Packet<BaseServer, MatchmakingClient> {
    public GameServerAuthPacket(MatchmakingClient client) {
        super(client);
    }

    @Override
    public void onWritePacket(MatchmakingClient client, Object... args) throws IOException {
        String secret = (String)args[0];
        long ID = (long)args[1];

        write((byte)0x23)
                .write(secret.length())
                .write(secret)
                .write(ID)
                .endTCP();
    }
}
