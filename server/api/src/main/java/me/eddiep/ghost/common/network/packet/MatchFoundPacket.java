package me.eddiep.ghost.common.network.packet;

import me.eddiep.ghost.common.network.BasePlayerClient;
import me.eddiep.ghost.common.network.BaseServer;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class MatchFoundPacket extends Packet<BaseServer, BasePlayerClient> {
    public MatchFoundPacket(BasePlayerClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(BasePlayerClient client, Object... args) throws IOException{
        if (args.length != 2)
            return;

        float startX = (float)args[0];
        float startY = (float)args[1];
        PlayableEntity[] enemies = (PlayableEntity[]) args[2];

        write((byte)0x02)
                .write(startX)
                .write(startY)
                .write(enemies.length);

        for (PlayableEntity entity : enemies) {
            write(entity.getName().length());
            write(entity.getName());
        }

        endTCP();
    }
}
