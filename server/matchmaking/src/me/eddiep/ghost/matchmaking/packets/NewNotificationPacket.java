package me.eddiep.ghost.matchmaking.packets;

import me.eddiep.ghost.matchmaking.TcpClient;
import me.eddiep.ghost.matchmaking.TcpServer;
import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.game.util.Notification;
import me.eddiep.ghost.game.util.Request;

import java.io.IOException;

public class NewNotificationPacket extends Packet<TcpServer, TcpClient> {
    public NewNotificationPacket(TcpClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(TcpClient client, Object... args) throws IOException {
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
