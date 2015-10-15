package me.eddiep.ghost.client.network.packets

import me.eddiep.ghost.client.network.Packet
import me.eddiep.ghost.client.network.PlayerClient

class ReadyPacket : Packet<PlayerClient>() {

    override fun write(vararg arg : Any) {
        val ready = arg[0] as Boolean

        write(0x03.toByte())
        write(ready)
        endTCP()
    }
}
