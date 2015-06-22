package me.eddiep.ghost.matchmaking.packets;

import me.eddiep.ghost.matchmaking.TcpClient;
import me.eddiep.ghost.matchmaking.TcpServer;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class MatchStatusPacket extends Packet<TcpServer, TcpClient> {
    public MatchStatusPacket(TcpClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(TcpClient client, Object... args) throws IOException {
        if (args.length == 0 || !(args[0] instanceof Boolean))
            return;

        Boolean status = (Boolean) args[0];
        String reason = (String)args[1];

        write((byte)0x06)
          .write(status)
          .write(reason.length())
          .write(reason)
          .endTCP();
    }
}
