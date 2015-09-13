package me.eddiep.ghost.gameserver.api.network.packets;

import me.eddiep.ghost.common.game.Player;
import me.eddiep.ghost.common.network.BaseServer;
import me.eddiep.ghost.game.stats.MatchHistory;
import me.eddiep.ghost.gameserver.api.network.MatchmakingClient;
import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.network.sql.PlayerData;

import java.io.IOException;
import java.util.List;

public class MatchHistoryPacket extends Packet<BaseServer, MatchmakingClient> {
    public MatchHistoryPacket(MatchmakingClient client) {
        super(client);
    }

    @Override
    public void onWritePacket(MatchmakingClient client, Object... args) throws IOException {
        MatchHistory history = (MatchHistory)args[0];
        List<Player> disconnects = (List<Player>)args[1];

        PlayerPacketObject[] dodgers = new PlayerPacketObject[disconnects.size()];
        if (disconnects.size() > 0) {
            for (int i = 0; i < dodgers.length; i++) {
                PlayerPacketObject obj = new PlayerPacketObject();
                obj.session = disconnects.get(i).getSession();
                obj.stats = disconnects.get(i).getStats();

                dodgers[i] = obj;
            }
        }

        write((byte) 0x27)
                .write(history)
                .write(dodgers.length != 0)
                .write(dodgers)
                .endTCPFlush();
    }

    public class PlayerPacketObject {
        private String session;
        private PlayerData stats;
    }
}
