package me.eddiep.ghost.matchmaking.network.packets;

import me.eddiep.ghost.matchmaking.network.PlayerClient;
import me.eddiep.ghost.matchmaking.network.TcpServer;
import me.eddiep.ghost.matchmaking.network.gameserver.Stream;
import me.eddiep.ghost.network.packet.Packet;

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
