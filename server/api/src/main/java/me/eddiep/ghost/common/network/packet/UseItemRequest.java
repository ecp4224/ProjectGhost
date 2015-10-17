package me.eddiep.ghost.common.network.packet;

import me.eddiep.ghost.common.game.Player;
import me.eddiep.ghost.common.network.BasePlayerClient;
import me.eddiep.ghost.common.network.BaseServer;
import me.eddiep.ghost.game.match.item.Item;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class UseItemRequest extends Packet<BaseServer, BasePlayerClient> {
    public UseItemRequest(BasePlayerClient client, byte[] data) {
        super(client, data);
    }

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