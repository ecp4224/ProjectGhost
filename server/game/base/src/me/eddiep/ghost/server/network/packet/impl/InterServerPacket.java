package me.eddiep.ghost.server.network.packet.impl;

import me.eddiep.ghost.server.Starter;
import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.network.dataserv.CentralServer;
import me.eddiep.ghost.server.network.dataserv.actions.Action;

import java.io.IOException;

public class InterServerPacket extends CentralServerPacket {
    private static final Action<?>[] ACTIONS = {

    };

    public InterServerPacket(Client client) {
        super(client);
    }

    @Override
    public void onHandlePacket(Client client) throws IOException {
        super.onHandlePacket(client);

        int actionSize = consume(4).asInt();
        int jsonSize = consume(4).asInt();

        String action = consume(actionSize).asString();
        String json = consume(jsonSize).asString();

        for (Action a : ACTIONS) {
            if (a.name().equals(action)) {
                Object obj = Starter.GSON.fromJson(json, a.dataClass());

                a.performAction((CentralServer) client, obj);
                break;
            }
        }
    }

    @Override
    public void onWritePacket(Client client, Object... args) throws IOException {
        super.onWritePacket(client, args);

        String action = (String) args[0];
        Object data = args[1];

        String json = Starter.GSON.toJson(data);

        write((byte)0xFF)
                .write(action.length())
                .write(json.length())
                .write(action)
                .write(json)
                .endTCP();
    }
}
