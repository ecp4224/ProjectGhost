package com.boxtrotstudio.ghost.common.network.packet;

import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.game.match.world.ParticleEffect;
import com.boxtrotstudio.ghost.network.packet.Packet;

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
