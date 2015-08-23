package me.eddiep.ghost.test.network.packet;

import me.eddiep.ghost.game.match.world.ParticleEffect;
import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.test.network.TcpUdpClient;
import me.eddiep.ghost.test.network.TcpUdpServer;

import java.io.IOException;

public class SpawnParticleEffectPacket extends Packet<TcpUdpServer, TcpUdpClient> {
    public SpawnParticleEffectPacket(TcpUdpClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(TcpUdpClient client, Object... args) throws IOException {

        ParticleEffect effect = (ParticleEffect) args[0];
        int duration = (int) args[1];
        int size = (int) args[2];
        float x = (float) args[3];
        float y = (float) args[4];
        double rotation = (double) args[5];

        write((byte) 0x30)
                .write(effect.getId())
                .write(duration)
                .write(size)
                .write(x)
                .write(y)
                .write(rotation)
                .endTCP();
    }
}
