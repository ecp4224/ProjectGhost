package me.eddiep.ghost.client.network.packets

import me.eddiep.ghost.client.network.Packet
import me.eddiep.ghost.client.network.PlayerClient

class ItemUsePacket : Packet<PlayerClient>() {
    override fun write(vararg args : Any) {
        val slot = args[0] as Byte

        client.sendCount++;

        write(0x39.toByte())
        write(client.sendCount)
        write(slot)
        endUdp()
    }
}
