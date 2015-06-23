package me.eddiep.ghost.test.network.packet;

import me.eddiep.ghost.game.entities.PlayableEntity;
import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.test.network.TcpUdpClient;
import me.eddiep.ghost.test.network.TcpUdpServer;

import java.io.IOException;

public class PlayerStatePacket extends Packet<TcpUdpServer, TcpUdpClient> {
    public PlayerStatePacket(TcpUdpClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(TcpUdpClient client, Object... args) throws IOException {
        if (args.length != 1)
            return;

        PlayableEntity player = (PlayableEntity)args[0];

        write((byte)0x12)
            .write(client.getPlayer().equals(player) ? (short)0 : player.getID())
            .write(player.getLives())
            .write(player.isDead())
            .write(player.isFrozen())
            .endTCP();
    }
}
