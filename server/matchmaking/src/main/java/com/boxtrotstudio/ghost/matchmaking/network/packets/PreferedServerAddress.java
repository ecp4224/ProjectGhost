package com.boxtrotstudio.ghost.matchmaking.network.packets;

import com.boxtrotstudio.ghost.matchmaking.network.PlayerClient;
import com.boxtrotstudio.ghost.matchmaking.core.hosts.gameserver.Stream;
import com.boxtrotstudio.ghost.network.packet.Packet;
import com.boxtrotstudio.ghost.matchmaking.network.TcpServer;

import java.io.IOException;
import java.net.InetAddress;

public class PreferedServerAddress extends Packet<TcpServer, PlayerClient> {
    public PreferedServerAddress(PlayerClient client) {
        super(client);
    }

    @Override
    public void onHandlePacket(PlayerClient client) throws IOException {
        byte[] bytes = consume(4).raw();

        if (client.getPlayer().getStream() != Stream.TEST)
            return;

        InetAddress address = InetAddress.getByAddress(bytes);
        client.getPlayer().setPreferedServer(address);
    }
}
