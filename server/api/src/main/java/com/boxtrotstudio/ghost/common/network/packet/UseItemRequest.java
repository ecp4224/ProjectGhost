package com.boxtrotstudio.ghost.common.network.packet;

import com.boxtrotstudio.ghost.common.game.Player;
import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.game.match.item.Item;
import com.boxtrotstudio.ghost.network.packet.Packet;

import java.io.IOException;

public class UseItemRequest extends Packet<BaseServer, BasePlayerClient> {

    @Override
    public void onHandlePacket(BasePlayerClient client) throws IOException {
        int packetNumber = consume(4).asInt();
        if (packetNumber < client.getLastReadPacket()) {
            int dif = client.getLastReadPacket() - packetNumber;
            if (dif >= Integer.MAX_VALUE - 1000) {
                client.setLastReadPacket(packetNumber);
            } else return;
        } else {
            client.setLastReadPacket(packetNumber);
        }

        byte slot = consume(1).asByte();

        if (client.getPlayer() != null && client.getPlayer().isInMatch()) {
            Player p = client.getPlayer();
            Item item = p.getInventory().getItem(slot);
            if (item != null) {
                item.activate(p);
                p.getInventory().remove(slot);
            }
        }
    }
}