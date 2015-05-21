package me.eddiep.ghost.server.network.dataserv.actions;

import me.eddiep.ghost.server.Starter;
import me.eddiep.ghost.server.network.dataserv.CentralServer;
import me.eddiep.ghost.server.network.packet.impl.InterServerPacket;

import java.io.IOException;

public class QueueInfoRequest implements Action<String> {
    @Override
    public String name() {
        return "queueInfoRequest";
    }

    @Override
    public Class<String> dataClass() {
        return String.class;
    }

    @Override
    public void performAction(CentralServer client, String object) {
        InterServerPacket packet = new InterServerPacket(client);
        try {
            packet.writePacket(name(), Starter.getGame().queueInfo());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
