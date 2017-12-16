package com.boxtrotstudio.ghost.matchmaking.network.packets;

import com.boxtrotstudio.ghost.matchmaking.core.hosts.gameserver.Stream;
import com.boxtrotstudio.ghost.matchmaking.network.GameServerClient;
import com.boxtrotstudio.ghost.matchmaking.network.TcpServer;
import com.boxtrotstudio.ghost.network.packet.Packet;

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
