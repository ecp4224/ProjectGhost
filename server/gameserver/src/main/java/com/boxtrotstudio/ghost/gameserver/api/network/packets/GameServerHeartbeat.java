package com.boxtrotstudio.ghost.gameserver.api.network.packets;

import com.boxtrotstudio.ghost.gameserver.api.network.MatchmakingClient;
import com.boxtrotstudio.ghost.network.packet.Packet;
import com.boxtrotstudio.ghost.common.network.BaseServer;

import java.io.IOException;

public class GameServerHeartbeat extends Packet<BaseServer, MatchmakingClient> {
    public GameServerHeartbeat(MatchmakingClient client) {
        super(client);
    }

    @Override
    public void onWritePacket(MatchmakingClient client, Object... args) throws IOException {
        short playerCount = (short)args[0];
        short matchCount = (short)args[1];
        boolean isFull = (boolean)args[2];
        long timePerTick = (long)args[3];

        write((byte)0x24)
                .write(playerCount)
                .write(matchCount)
                .write(isFull)
                .write(timePerTick)
                .endTCP();
    }
}
