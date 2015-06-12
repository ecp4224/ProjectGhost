package me.eddiep.ghost.server.network.packet.impl;

import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.network.packet.Packet;

public class EntityMetadataPacket extends Packet {
    public EntityMetadataPacket(Client client) {
        super(client);
    }

    @Override
    public void onWritePacket(Client client, Object... obj) {

    }
}
