package com.boxtrotstudio.ghost.common.network.packet;

import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.game.match.world.map.WorldMap;
import com.boxtrotstudio.ghost.network.packet.Packet;

import java.io.IOException;

public class MapSettingsPacket extends Packet<BaseServer, BasePlayerClient> {
    public MapSettingsPacket(BasePlayerClient client) {
        super(client);
    }

    @Override
    public void onWritePacket(BasePlayerClient client, Object... args) throws IOException {

        WorldMap map = (WorldMap)args[0];

        if (map != null) {

            int[] color = map.getAmbientColor();
            write((byte) 0x35)
                    .write(map.getAmbientPower())
                    .write(color[0])
                    .write(color[1])
                    .write(color[2])
                    .write(map.getName().length())
                    .write(map.getName())
                    .endTCP();
        } else {
            write((byte) 0x35)
                    .write(1f)
                    .write(255)
                    .write(255)
                    .write(255)
                    .write("Default".length())
                    .write("Default")
                    .endTCP();
        }
    }
}
