package me.eddiep.ghost.central.network.packet.impl;

import me.eddiep.ghost.central.network.Client;
import me.eddiep.ghost.central.network.packet.Packet;
import me.eddiep.ghost.central.utils.Notification;
import me.eddiep.ghost.central.utils.Request;

import java.io.IOException;

public class NewNotificationPacket extends Packet {
    public NewNotificationPacket(Client client) {
        super(client);
    }

    @Override
    protected void onWritePacket(Client client, Object... args) throws IOException {
        if (args.length != 1)
            return;

        Notification notification = (Notification)args[0];

        write((byte)0x15)
                .write(notification.getId())
                .write(notification instanceof Request)
                .write(notification.getTitle().length())
                .write(notification.getDescription().length())
                .write(notification.getTitle())
                .write(notification.getDescription())
                .endTCP();
    }
}
