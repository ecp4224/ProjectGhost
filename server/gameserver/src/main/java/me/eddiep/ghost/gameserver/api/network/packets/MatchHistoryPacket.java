package me.eddiep.ghost.gameserver.api.network.packets;

import me.eddiep.ghost.common.network.BaseServer;
import me.eddiep.ghost.game.stats.MatchHistory;
import me.eddiep.ghost.gameserver.api.network.MatchmakingClient;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class MatchHistoryPacket extends Packet<BaseServer, MatchmakingClient> {
    public MatchHistoryPacket(MatchmakingClient client) {
        super(client);
    }

    @Override
    public void onWritePacket(MatchmakingClient client, Object... args) throws IOException {
        MatchHistory history = (MatchHistory)args[0];

        write((byte) 0x27)
                .write(history)
                .endTCPFlush();
    }
}
