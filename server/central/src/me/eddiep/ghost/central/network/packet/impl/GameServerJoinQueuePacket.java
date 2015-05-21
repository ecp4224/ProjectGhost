package me.eddiep.ghost.central.network.packet.impl;

import me.eddiep.ghost.central.network.Client;

import java.io.IOException;

public class GameServerJoinQueuePacket extends GameServerPacket {
    public GameServerJoinQueuePacket(Client client) {
        super(client);
    }
}
