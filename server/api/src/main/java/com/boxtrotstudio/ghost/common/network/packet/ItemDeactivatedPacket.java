package com.boxtrotstudio.ghost.common.network.packet;

import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.network.packet.Packet;

import java.io.IOException;

public class ItemDeactivatedPacket extends Packet<BaseServer, BasePlayerClient> {
    public ItemDeactivatedPacket(BasePlayerClient client) {
        super(client);
    }

    @Override
    public void onWritePacket(BasePlayerClient client, Object... args) throws IOException {

        short type = (short) args[0];
        short id = (short)args[1];

        write((byte)0x33)
                .write(type)
                .write(id)
                .endTCP();
    }
}
