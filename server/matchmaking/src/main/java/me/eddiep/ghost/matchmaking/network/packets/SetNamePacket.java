package me.eddiep.ghost.matchmaking.network.packets;

import me.eddiep.ghost.matchmaking.Main;
import me.eddiep.ghost.matchmaking.network.PlayerClient;
import me.eddiep.ghost.matchmaking.network.TcpServer;
import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.network.validate.DummyValidator;

import java.io.IOException;

public class SetNamePacket extends Packet<TcpServer, PlayerClient> {
    public SetNamePacket(PlayerClient client, byte[] data) {
        super(client, data);
    }

    @Override
    public void onHandlePacket(PlayerClient client)  throws IOException {
        String name = consume(255).asString();

        if (Main.SESSION_VALIDATOR instanceof DummyValidator) {
            client.getPlayer().setName(name.trim());
        }
    }
}
