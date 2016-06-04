package com.boxtrotstudio.ghost.matchmaking.network.packets;

import com.boxtrotstudio.ghost.matchmaking.network.PlayerClient;
import com.boxtrotstudio.ghost.matchmaking.network.TcpServer;
import com.boxtrotstudio.ghost.network.packet.Packet;

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
