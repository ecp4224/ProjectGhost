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
        long ID = (long)args[1];

        write((byte)0x37)
                .write(ID)
                .write(light.getX())
                .write(light.getY())
                .write(light.getRadius())
                .write(light.getIntensity())
                .write(light.getColor().getRGB())
                .endTCP();
    }
}
