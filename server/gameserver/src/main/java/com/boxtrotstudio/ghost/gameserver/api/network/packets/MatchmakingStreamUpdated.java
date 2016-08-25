package com.boxtrotstudio.ghost.gameserver.api.network.packets;

import com.boxtrotstudio.ghost.gameserver.api.GameServer;
import com.boxtrotstudio.ghost.gameserver.api.Stream;
import com.boxtrotstudio.ghost.gameserver.api.network.MatchmakingClient;
import com.boxtrotstudio.ghost.network.packet.Packet;
import com.boxtrotstudio.ghost.common.game.MatchFactory;
import com.boxtrotstudio.ghost.common.network.BaseServer;

import java.io.IOException;

public class MatchmakingStreamUpdated extends Packet<BaseServer, MatchmakingClient> {

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
