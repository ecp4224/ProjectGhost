package me.eddiep.ghost.client.network.packets

import me.eddiep.ghost.client.network.Packet
import me.eddiep.ghost.client.network.PlayerClient

class UpdateInventoryPacket : Packet<PlayerClient>() {

    override fun handle() {
        val type = consume(2).asShort();
        val slot = consume(1).asByte();


    }
}

