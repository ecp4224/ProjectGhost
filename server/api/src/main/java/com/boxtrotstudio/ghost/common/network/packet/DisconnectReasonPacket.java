package com.boxtrotstudio.ghost.common.network.packet;

import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.network.packet.Packet;
import com.boxtrotstudio.ghost.common.network.BaseServer;

import java.io.IOException;

public class DisconnectReasonPacket extends Packet<BaseServer, BasePlayerClient> {

    public DisconnectReasonPacket(BasePlayerClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(BasePlayerClient client, Object... args) throws IOException {
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
