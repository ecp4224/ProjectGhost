package me.eddiep.ghost.common.network.packet;

import me.eddiep.ghost.common.network.BasePlayerClient;
import me.eddiep.ghost.common.network.BaseServer;
import me.eddiep.ghost.game.match.world.map.WorldMap;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class MapSettingsPacket extends Packet<BaseServer, BasePlayerClient> {
    public MapSettingsPacket(BasePlayerClient client) {
        super(client);
    }

    @Override
    public void onWritePacket(BasePlayerClient client, Object... args) throws IOException {

        WorldMap map = (WorldMap)args[0];

        int[] color = map.getAmbiantColor();
        write((byte)0x35)
                .write(map.getAmbiantPower())
                .write(color[0])
                .write(color[1])
                .write(color[2])
                .write(map.getName().length())
                .write(map.getName())
                .endTCP();
    }
}
