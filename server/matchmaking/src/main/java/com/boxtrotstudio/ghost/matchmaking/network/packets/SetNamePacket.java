package com.boxtrotstudio.ghost.matchmaking.network.packets;

import com.boxtrotstudio.ghost.matchmaking.Main;
import com.boxtrotstudio.ghost.matchmaking.network.PlayerClient;
import com.boxtrotstudio.ghost.matchmaking.network.TcpServer;
import com.boxtrotstudio.ghost.network.packet.Packet;
import com.boxtrotstudio.ghost.network.validate.DummyValidator;

import java.io.IOException;

public class SetNamePacket extends Packet<TcpServer, PlayerClient> {

    @Override
    public void onHandlePacket(PlayerClient client)  throws IOException {
        String name = consume(255).asString();

        if (Main.SESSION_VALIDATOR instanceof DummyValidator) {
            client.getPlayer().setName(name.trim());
            client.getPlayer().setPlayerID(name.trim().hashCode());
        }
    }
}
