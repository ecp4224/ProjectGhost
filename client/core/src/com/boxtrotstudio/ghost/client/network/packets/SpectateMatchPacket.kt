package com.boxtrotstudio.ghost.client.network.packets

import com.boxtrotstudio.ghost.client.network.Packet
import com.boxtrotstudio.ghost.client.network.PlayerClient

class SpectateMatchPacket : Packet<PlayerClient>() {

    override fun write(vararg args : Any) {
        val id = args[0] as Long;

        write(0x28.toByte())
        write(id)
        endTCP()
    }
}
