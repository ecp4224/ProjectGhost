package com.boxtrotstudio.ghost.matchmaking.network.packets;

import com.boxtrotstudio.ghost.matchmaking.network.PlayerClient;
import com.boxtrotstudio.ghost.matchmaking.network.TcpServer;
import com.boxtrotstudio.ghost.network.notifications.Notification;
import com.boxtrotstudio.ghost.network.notifications.Request;
import com.boxtrotstudio.ghost.network.packet.Packet;

import java.io.IOException;

public class NewNotificationPacket extends Packet<TcpServer, PlayerClient> {
    public NewNotificationPacket(PlayerClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(PlayerClient client, Object... args) throws IOException {
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
