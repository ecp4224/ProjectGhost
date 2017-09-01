package com.boxtrotstudio.ghost.common.network.packet;

import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.game.match.LiveMatchImpl;
import com.boxtrotstudio.ghost.network.packet.Packet;

import java.io.IOException;

import static com.boxtrotstudio.ghost.utils.Global.RANDOM;

public class ChangeItemPacket extends Packet<BaseServer, BasePlayerClient> {
    @Override
    public void onHandlePacket(BasePlayerClient client) throws IOException {
        byte action = consume(1).asByte();

        if (client.getPlayer() == null || client.getPlayer().isInMatch())
            return;


        if (action == 0x10 || action < 0 || action >= LiveMatchImpl.ITEMS.length) {
            client.getPlayer().setPreferredItem(RANDOM.nextInt(LiveMatchImpl.ITEMS.length));
        } else {
            client.getPlayer().setPreferredItem(action);
        }
    }
}
