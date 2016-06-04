package com.boxtrotstudio.ghost.common.network.packet;

import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.game.match.world.timeline.PlayableSnapshot;
import com.boxtrotstudio.ghost.network.packet.Packet;
import com.boxtrotstudio.ghost.common.network.BaseServer;

import java.io.IOException;

public class PlayerStatePacket extends Packet<BaseServer, BasePlayerClient> {
    public PlayerStatePacket(BasePlayerClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(BasePlayerClient client, Object... args) throws IOException {
        if (args.length != 1)
            return;

        PlayableSnapshot player = (PlayableSnapshot)args[0];

        write((byte)0x12)
            .write(client.getPlayer().getID() == player.getID() ? (short)0 : player.getID())
            .write(player.getLives())
            .write(player.isDead())
            .write(player.isFrozen())
            .write(player.isInvincible())
            .endTCP();
    }
}
