package com.boxtrotstudio.ghost.common.network.item;

import com.boxtrotstudio.ghost.common.game.User;
import com.boxtrotstudio.ghost.common.network.packet.UpdateInventoryPacket;
import com.boxtrotstudio.ghost.game.match.item.Inventory;
import com.boxtrotstudio.ghost.game.match.item.Item;

import java.io.IOException;

public class NetworkInventory extends Inventory {
    private final UpdateInventoryPacket updatePacket;
    private final User owner;

    public NetworkInventory(int size, User user) {
        super(size);

        updatePacket = new UpdateInventoryPacket(user.getClient());
        this.owner = user;
    }

    @Override
    public int addItem(Item item) {
        int slot = super.addItem(item);

        updatePacket.reuseFor(owner.getClient());
        try {
            updatePacket.writePacket(item.getEntity().getType(), (byte)slot);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return slot;
    }

    @Override
    public void set(Item item, int slot) {
        super.set(item, slot);

        updatePacket.reuseFor(owner.getClient());
        try {
            short type;
            if (item != null)
                type = item.getEntity().getType();
            else
                type = -1;
            updatePacket.writePacket(type, (byte)slot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
