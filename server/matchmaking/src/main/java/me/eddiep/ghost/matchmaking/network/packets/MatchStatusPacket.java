package me.eddiep.ghost.matchmaking.network.packets;

import main.java.matchmaking.network.PlayerClient;
import main.java.matchmaking.network.TcpServer;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class MatchStatusPacket extends Packet<TcpServer, PlayerClient> {
    public MatchStatusPacket(PlayerClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(PlayerClient client, Object... args) throws IOException {
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
