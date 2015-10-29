package me.eddiep.ghost.gameserver.api.network.packets;

import me.eddiep.ghost.common.network.BaseServer;
import me.eddiep.ghost.gameserver.api.network.MatchmakingClient;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class MatchmakingOkPacket extends Packet<BaseServer, MatchmakingClient> {
    public MatchmakingOkPacket(MatchmakingClient client) {
        super(client);
    }

    @Override
    public void onHandlePacket(MatchmakingClient client)  throws IOException {
        boolean isOk = consume(1).asBoolean();

        client.receiveOk(isOk);
    }

    @Override
    public void onWritePacket(MatchmakingClient client, Object... args) throws IOException {
        boolean isOk = (boolean)args[0];

        write((byte)0x90)
                .write(isOk)
                .endTCP();
    }
}
