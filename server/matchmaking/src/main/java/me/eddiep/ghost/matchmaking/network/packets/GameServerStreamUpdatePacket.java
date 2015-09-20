package me.eddiep.ghost.matchmaking.network.packets;

import me.eddiep.ghost.matchmaking.network.GameServerClient;
import me.eddiep.ghost.matchmaking.network.TcpServer;
import me.eddiep.ghost.matchmaking.network.gameserver.Stream;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class GameServerStreamUpdatePacket extends Packet<TcpServer, GameServerClient> {
    public GameServerStreamUpdatePacket(GameServerClient client) {
        super(client);
    }

    @Override
    public void onWritePacket(GameServerClient client, Object... args) throws IOException {
        Stream newStream = (Stream)args[0];

        write((byte)0x36)
                .write(newStream.getLevel())
                .endTCP();
    }
}
