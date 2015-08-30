package me.eddiep.ghost.common.network.packet;

import me.eddiep.ghost.common.network.BasePlayerClient;
import me.eddiep.ghost.common.network.BaseServer;
import me.eddiep.ghost.game.match.world.timeline.PlayableSnapshot;
import me.eddiep.ghost.network.packet.Packet;

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
            .endTCP();
    }
}
