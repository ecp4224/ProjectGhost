package me.eddiep.ghost.gameserver.api.network.packets;

import me.eddiep.ghost.gameserver.api.network.TcpUdpClient;
import me.eddiep.ghost.gameserver.api.network.TcpUdpServer;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class OkPacket extends Packet<TcpUdpServer, TcpUdpClient> {
    public OkPacket(TcpUdpClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(TcpUdpClient client, Object... args) throws IOException {
        if (args.length == 0)
            return;

        boolean isOk = (boolean)args[0];

        write((byte)0x01)
                .write(isOk)
                .endTCP();
    }
}
