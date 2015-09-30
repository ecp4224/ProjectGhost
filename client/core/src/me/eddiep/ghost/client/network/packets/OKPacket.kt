package me.eddiep.ghost.client.network.packets

import me.eddiep.ghost.client.network.Packet
import me.eddiep.ghost.client.network.PlayerClient

class OKPacket() : Packet<PlayerClient>() {

    override fun handle() {
        val isOk : Boolean = consume(1).asBoolean()
    }
}
