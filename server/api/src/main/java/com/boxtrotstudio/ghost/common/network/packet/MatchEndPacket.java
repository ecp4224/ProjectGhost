package com.boxtrotstudio.ghost.common.network.packet;

import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.network.packet.Packet;

import java.io.IOException;

public class MatchEndPacket extends Packet<BaseServer, BasePlayerClient> {
    public MatchEndPacket(BasePlayerClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(BasePlayerClient client, Object... args) throws IOException {
        if (args.length != 2)
            return;

        boolean winrar = (boolean) args[0];
        long matchId = (long)args[1];

        write((byte)0x07)
                .write(winrar)
                .write(matchId)
                .write(client.getPlayer().getCurrentMatchStats())
                .endTCP();
    }
}
