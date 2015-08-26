package me.eddiep.ghost.gameserver.api.network.packets;

import me.eddiep.ghost.network.notifications.Notification;
import me.eddiep.ghost.network.notifications.Request;
import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.gameserver.api.network.TcpUdpClient;
import me.eddiep.ghost.gameserver.api.network.TcpUdpServer;

import java.io.IOException;

public class NewNotificationPacket extends Packet<TcpUdpServer, TcpUdpClient> {
    public NewNotificationPacket(TcpUdpClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(TcpUdpClient client, Object... args) throws IOException {
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
