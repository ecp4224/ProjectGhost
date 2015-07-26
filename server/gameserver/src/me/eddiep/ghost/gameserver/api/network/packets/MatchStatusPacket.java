package me.eddiep.ghost.gameserver.api.network.packets;

import me.eddiep.ghost.gameserver.api.network.TcpUdpClient;
import me.eddiep.ghost.gameserver.api.network.TcpUdpServer;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class MatchStatusPacket extends Packet<TcpUdpServer, TcpUdpClient> {
    public MatchStatusPacket(TcpUdpClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(TcpUdpClient client, Object... args) throws IOException {
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