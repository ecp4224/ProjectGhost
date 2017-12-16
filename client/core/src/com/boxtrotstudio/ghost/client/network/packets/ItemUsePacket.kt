package com.boxtrotstudio.ghost.client.network.packets

import com.boxtrotstudio.ghost.client.network.Packet
import com.boxtrotstudio.ghost.client.network.PlayerClient

class ItemUsePacket : Packet<PlayerClient>() {
    override fun write(vararg args : Any) {
        val slot = args[0] as Byte

        client.sendCount++

        write(0x39.toByte())
        write(client.sendCount)
        write(slot)
        endUdp()
    }
}
