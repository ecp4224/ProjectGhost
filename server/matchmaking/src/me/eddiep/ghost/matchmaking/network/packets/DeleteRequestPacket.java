package me.eddiep.ghost.matchmaking.network.packets;

import me.eddiep.ghost.game.util.Request;
import me.eddiep.ghost.matchmaking.network.PlayerClient;
import me.eddiep.ghost.matchmaking.network.TcpServer;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class DeleteRequestPacket extends Packet<TcpServer, PlayerClient> {
    public DeleteRequestPacket(PlayerClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(PlayerClient client, Object... args) throws IOException {
        if (args.length != 1)
            return;

        Request request = (Request)args[0];

        write((byte)0x16)
                .write(request.getId())
                .endTCP();
    }
}
