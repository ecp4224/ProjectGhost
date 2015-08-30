package me.eddiep.ghost.common.network.packet;

import me.eddiep.ghost.common.network.BasePlayerClient;
import me.eddiep.ghost.common.network.BaseServer;
import me.eddiep.ghost.game.match.world.ParticleEffect;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class SpawnParticleEffectPacket extends Packet<BaseServer, BasePlayerClient> {
    public SpawnParticleEffectPacket(BasePlayerClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(BasePlayerClient client, Object... args) throws IOException {

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
