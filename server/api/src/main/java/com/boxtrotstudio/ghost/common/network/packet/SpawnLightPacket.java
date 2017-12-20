package com.boxtrotstudio.ghost.common.network.packet;

import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.game.match.world.map.Light;
import com.boxtrotstudio.ghost.network.packet.Packet;

import java.io.IOException;

public class SpawnLightPacket extends Packet<BaseServer, BasePlayerClient> {
    public SpawnLightPacket(BasePlayerClient client) {
        super(client);
    }

    @Override
    public void onWritePacket(BasePlayerClient client, Object... args) throws IOException {
        Light light = (Light) args[0];
        short ID = (short)args[1];

        int rgba888 = light.getColor888();

        write((byte)0x37)
                .write(ID)
                .write(light.getX())
                .write(light.getY())
                .write(light.getRadius())
                .write(light.getIntensity())
                .write(rgba888)
                .write(light.doesCastShadows())
                .write(light.isConeLight());

        if (light.isConeLight()) {
            write(light.getDirectionDegrees());
            write(light.getConeDegrees());
        }

        endTCP();
    }
}
