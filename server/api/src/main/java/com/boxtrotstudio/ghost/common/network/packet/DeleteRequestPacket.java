package com.boxtrotstudio.ghost.common.network.packet;

import com.boxtrotstudio.ghost.network.notifications.Request;
import com.boxtrotstudio.ghost.network.packet.Packet;
import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.common.network.BaseServer;

import java.io.IOException;

public class DeleteRequestPacket extends Packet<BaseServer, BasePlayerClient> {
    public DeleteRequestPacket(BasePlayerClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(BasePlayerClient client, Object... args) throws IOException {
        if (args.length != 1)
            return;

        Request request = (Request)args[0];

        write((byte)0x16)
                .write(request.getId())
                .endTCP();
    }
}
