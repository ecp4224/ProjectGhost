package me.eddiep.ghost.gameserver.api.network.packets;

import me.eddiep.ghost.common.game.MatchFactory;
import me.eddiep.ghost.common.network.BaseServer;
import me.eddiep.ghost.gameserver.api.GameServer;
import me.eddiep.ghost.gameserver.api.Stream;
import me.eddiep.ghost.gameserver.api.network.MatchmakingClient;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class MatchmakingStreamUpdated extends Packet<BaseServer, MatchmakingClient> {
    public MatchmakingStreamUpdated(MatchmakingClient client) {
        super(client);
    }

    @Override
    public void onHandlePacket(MatchmakingClient client) throws IOException {
        int newLevel = consume(4).asInt();

        Stream stream = Stream.fromInt(newLevel);

        GameServer.currentStream = stream;

        if (stream == Stream.BUFFERED) {
            System.err.println("This server hsa been moved to the buffered stream!");
            if (MatchFactory.getCreator().getAllActiveMatches().size() > 0) {
                System.err.println("This server will shutdown when all games finish..");
            } else {
                System.err.println("This server will now shutdown!");
                GameServer.stopServer();

                System.exit(0);
            }
        }
    }
}
