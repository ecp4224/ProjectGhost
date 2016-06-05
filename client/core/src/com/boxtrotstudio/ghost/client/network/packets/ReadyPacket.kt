package com.boxtrotstudio.ghost.client.network.packets

import com.boxtrotstudio.ghost.client.network.Packet
import com.boxtrotstudio.ghost.client.network.PlayerClient

class ReadyPacket : Packet<PlayerClient>() {

    override fun write(vararg arg : Any) {
        val ready = arg[0] as Boolean

        write(0x03.toByte())
        write(ready)
        endTCP()
    }
}
