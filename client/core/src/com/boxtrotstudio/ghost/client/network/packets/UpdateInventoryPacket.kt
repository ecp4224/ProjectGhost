package com.boxtrotstudio.ghost.client.network.packets

import com.boxtrotstudio.ghost.client.network.Packet
import com.boxtrotstudio.ghost.client.network.PlayerClient

class UpdateInventoryPacket : Packet<PlayerClient>() {

    override fun handle() {
        val type = consume(2).asShort();
        val slot = consume(1).asByte();

        if (type == (-1).toShort()) {
            client.game.player1.inventory.clearSlot(slot.toInt())
        } else {
            if (slot == 0.toByte()) {
                client.game.player1.inventory.setSlot1(type)
            } else {
                client.game.player1.inventory.setSlot2(type)
            }
        }
    }
}

