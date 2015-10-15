package me.eddiep.ghost.client.network.packets

import me.eddiep.ghost.client.network.Packet
import me.eddiep.ghost.client.network.PlayerClient

class SpectateMatchPacket : Packet<PlayerClient>() {

    override fun write(vararg args : Any) {
        val id = args[0] as Long;

        write(0x28.toByte())
        write(id)
        endTCP()
    }
}
