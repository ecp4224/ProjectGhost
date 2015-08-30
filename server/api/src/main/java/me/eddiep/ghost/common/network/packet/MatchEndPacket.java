package me.eddiep.ghost.common.network.packet;

import me.eddiep.ghost.common.network.BasePlayerClient;
import me.eddiep.ghost.common.network.BaseServer;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class MatchEndPacket extends Packet<BaseServer, BasePlayerClient> {
    public MatchEndPacket(BasePlayerClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(BasePlayerClient client, Object... args) throws IOException {
        if (args.length != 2)
            return;

        Boolean winrar = (Boolean) args[0];
        Long matchId = (Long)args[1];

        write((byte)0x07)
                .write(winrar)
                .write(matchId)
                .endTCP();
    }
}
