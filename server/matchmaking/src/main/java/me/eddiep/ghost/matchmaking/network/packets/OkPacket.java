package me.eddiep.ghost.matchmaking.network.packets;

import main.java.matchmaking.network.TcpClient;
import main.java.matchmaking.network.TcpServer;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class OkPacket extends Packet<TcpServer, TcpClient> {
    public OkPacket(TcpClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(TcpClient client, Object... args) throws IOException {
        if (args.length == 0)
            return;

        boolean isOk = (boolean)args[0];

        write((byte)0x01)
                .write(isOk)
                .endTCP();
    }
}
