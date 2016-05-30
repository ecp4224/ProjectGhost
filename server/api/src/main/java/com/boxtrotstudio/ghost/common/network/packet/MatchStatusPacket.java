package com.boxtrotstudio.ghost.common.network.packet;

import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.network.packet.Packet;
import com.boxtrotstudio.ghost.common.network.BaseServer;

import java.io.IOException;

public class MatchStatusPacket extends Packet<BaseServer, BasePlayerClient> {
    public MatchStatusPacket(BasePlayerClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(BasePlayerClient client, Object... args) throws IOException {
        if (args.length == 0 || !(args[0] instanceof Boolean))
            return;

        Boolean status = (Boolean) args[0];
        String reason = (String)args[1];
        if (reason == null)
            reason = "";

        write((byte)0x06)
          .write((boolean)status)
          .write(reason.length())
          .write(reason)
          .endTCP();
    }
}
