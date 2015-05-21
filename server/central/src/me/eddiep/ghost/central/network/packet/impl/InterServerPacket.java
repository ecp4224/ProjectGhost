package me.eddiep.ghost.central.network.packet.impl;

import me.eddiep.ghost.central.Main;
import me.eddiep.ghost.central.network.Client;
import me.eddiep.ghost.central.network.gameserv.GameServerConnection;
import me.eddiep.ghost.central.network.gameserv.actions.Action;
import me.eddiep.ghost.central.network.gameserv.actions.QueueInfoAction;

import java.io.IOException;

public class InterServerPacket extends GameServerPacket {
    private static final Action<?>[] ACTIONS = {
            new QueueInfoAction()
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
            if (a.actionName().equals(action)) {
                Object obj = Main.GSON.fromJson(json, a.dataClass());

                a.performAction((GameServerConnection) client, obj);
                break;
            }
        }
    }

    @Override
    public void onWritePacket(Client client, Object... args) throws IOException {
        super.onWritePacket(client, args);

        String action = (String) args[0];
        Object data = args[1];

        String json = Main.GSON.toJson(data);

        write((byte)0xFF)
                .write(action.length())
                .write(json.length())
                .write(action)
                .write(json)
                .endTCP();
    }
}
