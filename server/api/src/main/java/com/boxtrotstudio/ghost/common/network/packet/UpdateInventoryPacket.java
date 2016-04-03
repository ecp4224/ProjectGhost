package com.boxtrotstudio.ghost.common.network.packet;

import com.boxtrotstudio.ghost.network.packet.Packet;
import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.common.network.BaseServer;

import java.io.IOException;

public class UpdateInventoryPacket extends Packet<BaseServer, BasePlayerClient> {
    public UpdateInventoryPacket(BasePlayerClient client) {
        super(client);
    }

    @Override
    public void onWritePacket(BasePlayerClient client, Object... args) throws IOException {
        short type = (short) args[0];
        byte slot = (byte) args[1];

        write((byte)0x38)
                .write(type)
                .write(slot)
                .endTCP();
    }
}
