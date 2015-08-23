package me.eddiep.ghost.gameserver.api.network.packets;

import me.eddiep.ghost.gameserver.api.network.TcpUdpClient;
import me.eddiep.ghost.gameserver.api.network.TcpUdpServer;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class ReadyPacket extends Packet<TcpUdpServer, TcpUdpClient> {

    public ReadyPacket(TcpUdpClient client) {
        super(client);
    }

    @Override
    public void onHandlePacket(TcpUdpClient client) throws IOException {
        client.getPlayer().setReady(consume().asBoolean());

        if (client.getPlayer().isReady()) {
            client.getPlayer().sendMatchMessage("Ready! Please wait for game to start.."); //TODO Change this message maybe..
        }
    }
}
