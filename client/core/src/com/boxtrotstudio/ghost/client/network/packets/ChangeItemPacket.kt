package com.boxtrotstudio.ghost.client.network.packets

import com.boxtrotstudio.ghost.client.network.Packet
import com.boxtrotstudio.ghost.client.network.PlayerClient

class ChangeItemPacket : Packet<PlayerClient>() {

    override fun write(vararg args : Any) {
        val item = args[0] as Byte

        write(0x50.toByte())
        write(item)
        endTCP()
    }
}
