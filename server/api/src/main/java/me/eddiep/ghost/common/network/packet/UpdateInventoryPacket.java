package me.eddiep.ghost.common.network.packet;

import me.eddiep.ghost.common.network.BasePlayerClient;
import me.eddiep.ghost.common.network.BaseServer;
import me.eddiep.ghost.network.packet.Packet;

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
