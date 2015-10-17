package me.eddiep.ghost.common.network.item;

import me.eddiep.ghost.common.game.User;
import me.eddiep.ghost.common.network.packet.UpdateInventoryPacket;
import me.eddiep.ghost.game.match.item.Inventory;
import me.eddiep.ghost.game.match.item.Item;

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
            updatePacket.writePacket(item.getEntity().getType(), slot);
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
            updatePacket.writePacket(item.getEntity().getType(), slot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
