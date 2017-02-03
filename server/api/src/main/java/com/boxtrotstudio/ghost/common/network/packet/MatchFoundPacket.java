package com.boxtrotstudio.ghost.common.network.packet;

import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.network.packet.Packet;

import java.io.IOException;

public class MatchFoundPacket extends Packet<BaseServer, BasePlayerClient> {
    public MatchFoundPacket(BasePlayerClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(BasePlayerClient client, Object... args) throws IOException{
        if (args.length != 4)
            return;

        float startX = (float)args[0];
        float startY = (float)args[1];
        PlayableEntity[] enemies = (PlayableEntity[]) args[2];
        PlayableEntity[] allies = (PlayableEntity[]) args[3];

        if (enemies == null || allies == null)
            return;

        write((byte)0x02)
                .write(startX)
                .write(startY)
                .write(client.getPlayer().getID())
                .write(client.getPlayer().currentAbility().id())
                .write(allies.length)
                .write(enemies.length);

        for (PlayableEntity entity : enemies) {
            write(entity.getName().length());
            write(entity.getName());
            write(entity.currentAbility().id());
        }

        for (PlayableEntity entity : allies) {
            write(entity.getName().length());
            write(entity.getName());
            write(entity.currentAbility().id());
        }

        endTCP();
    }
}
