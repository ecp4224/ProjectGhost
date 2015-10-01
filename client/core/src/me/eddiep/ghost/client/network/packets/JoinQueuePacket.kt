package me.eddiep.ghost.client.network.packets

import me.eddiep.ghost.client.network.Packet
import me.eddiep.ghost.client.network.PlayerClient

class JoinQueuePacket : Packet<PlayerClient>() {

    override fun write(vararg args : Any) {
        val queue = args[0] as Byte;

        write(0x05.toByte())
        write(queue)
        endTCP()
    }
}
