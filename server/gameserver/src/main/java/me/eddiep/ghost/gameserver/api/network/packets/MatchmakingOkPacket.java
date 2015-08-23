package me.eddiep.ghost.gameserver.api.network.packets;

import me.eddiep.ghost.gameserver.api.network.MatchmakingClient;
import me.eddiep.ghost.gameserver.api.network.TcpUdpServer;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class MatchmakingOkPacket extends Packet<TcpUdpServer, MatchmakingClient> {
    public MatchmakingOkPacket(MatchmakingClient client) {
        super(client);
    }

    @Override
    public void onHandlePacket(MatchmakingClient client)  throws IOException {
        boolean isOk = consume(1).asBoolean();

        client.receiveOk(isOk);
    }
}