package me.eddiep.ghost.matchmaking.network.packets;

import me.eddiep.ghost.matchmaking.network.PlayerClient;
import me.eddiep.ghost.matchmaking.network.TcpClient;
import me.eddiep.ghost.matchmaking.network.TcpServer;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class UpdateSessionPacket extends Packet<TcpServer, PlayerClient> {
    public UpdateSessionPacket(PlayerClient client) {
        super(client);
    }

    @Override
    public void onWritePacket(PlayerClient client, Object... args) throws IOException {

        write((byte) 0x29)
                .write(client.getPlayer().getSession().length())
                .write(client.getPlayer().getSession())
                .endTCP();
    }
}
