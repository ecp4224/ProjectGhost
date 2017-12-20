package com.boxtrotstudio.ghost.common.network.packet;

import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.game.match.stats.Stat;
import com.boxtrotstudio.ghost.network.packet.Packet;

import java.io.IOException;

public class StatUpdatePacket extends Packet<BaseServer, BasePlayerClient> {

    public StatUpdatePacket(BasePlayerClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(BasePlayerClient client, Object... args) throws IOException {
        if (args.length != 1 || !(args[0] instanceof Stat)) {
            return;
        }

        Stat stat = (Stat) args[0];
        String id = stat.getId();
        double value = stat.getValue();

        if (id.length() > 4) {
            throw new IllegalArgumentException("Status string should not be more than 4 bytes long.");
        } else if (id.length() < 4) {
            id = id + "    ".substring(id.length());
        }

        write((byte) 0x31)
                .write(id)
                .write(value)
                .endTCP();
    }
}
