package com.boxtrotstudio.ghost.common.network.packet;

import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.game.match.entities.map.Text;
import com.boxtrotstudio.ghost.network.packet.Packet;

import java.io.IOException;

public class DisplayTextPacket extends Packet<BaseServer, BasePlayerClient> {

    public DisplayTextPacket(BasePlayerClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(BasePlayerClient client, Object... args) throws IOException {
        Text text = (Text) args[0];

        write((byte)0x43)
                .write(text.getText().length())
                .write(text.getText())
                .write(text.getSize())
                .write(text.getColor().getRGB())
                .write(text.getPosition().x)
                .write(text.getPosition().y)
                .write(text.getTextOptions())
                .write(text.getID())
                .endTCP();
    }
}
