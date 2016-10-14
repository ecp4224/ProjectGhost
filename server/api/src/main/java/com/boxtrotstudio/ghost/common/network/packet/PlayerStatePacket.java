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

        byte lives = player.getLives();
        if (!player.showLives())
            lives = 0; //Hide lives from player

        write((byte)0x12)
            .write(client.getPlayer().getID() == player.getID() ? (short)0 : player.getID())
            .write(lives)
            .write(player.isDead())
            .write(player.isFrozen())
            .write(player.isInvincible())
            .endTCP();
    }
}
