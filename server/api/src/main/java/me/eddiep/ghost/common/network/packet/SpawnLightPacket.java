package me.eddiep.ghost.common.network.packet;

import me.eddiep.ghost.common.network.BasePlayerClient;
import me.eddiep.ghost.common.network.BaseServer;
import me.eddiep.ghost.game.match.world.map.Light;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class SpawnLightPacket extends Packet<BaseServer, BasePlayerClient> {
    public SpawnLightPacket(BasePlayerClient client) {
        super(client);
    }

    @Override
    public void onWritePacket(BasePlayerClient client, Object... args) throws IOException {
        Light light = (Light) args[0];
        short ID = (short)args[1];

        int rgba888 = (light.getColor().getRed() << 24) |
                (light.getColor().getGreen() << 16) |
                (light.getColor().getBlue() << 8) |
                light.getColor().getAlpha();

        write((byte)0x37)
                .write(ID)
                .write(light.getX())
                .write(light.getY())
                .write(light.getRadius())
                .write(light.getIntensity())
                .write(rgba888)
                .endTCP();
    }
}
