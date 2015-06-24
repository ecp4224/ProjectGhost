package me.eddiep.ghost.matchmaking.network.packets;

import me.eddiep.ghost.matchmaking.network.PlayerClient;
import me.eddiep.ghost.matchmaking.network.TcpServer;
import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.game.util.Notification;
import me.eddiep.ghost.game.util.Request;

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
