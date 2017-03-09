package com.boxtrotstudio.ghost.common.network.packet;

import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.game.match.entities.map.Text;
import com.boxtrotstudio.ghost.network.packet.Packet;

import java.io.IOException;

public class RemoveTextPacket extends Packet<BaseServer, BasePlayerClient> {

    public RemoveTextPacket(BasePlayerClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(BasePlayerClient client, Object... args) throws IOException {
        Text text = (Text) args[0];

        write((byte)0x44)
                .write(text.getID())
                .endTCP();
    }
}
