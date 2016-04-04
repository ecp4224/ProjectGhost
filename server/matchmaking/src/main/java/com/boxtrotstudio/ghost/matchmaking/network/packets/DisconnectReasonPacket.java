package com.boxtrotstudio.ghost.matchmaking.network.packets;


import com.boxtrotstudio.ghost.matchmaking.network.PlayerClient;
import com.boxtrotstudio.ghost.matchmaking.network.TcpServer;
import com.boxtrotstudio.ghost.network.packet.Packet;

import java.io.IOException;

public class DisconnectReasonPacket extends Packet<TcpServer, PlayerClient> {
    public DisconnectReasonPacket(PlayerClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(PlayerClient client, Object... args) throws IOException {
        String reason;
        if (args.length == 0 || !(args[0] instanceof String))
            reason = "No reason specified";
        else
            reason = (String)args[0];

        write((byte)0x42)
                .write((byte)reason.length())
                .write(reason)
                .endTCP();
    }
}
