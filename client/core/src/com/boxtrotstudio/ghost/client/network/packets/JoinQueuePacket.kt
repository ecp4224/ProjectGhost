package com.boxtrotstudio.ghost.client.network.packets

import com.boxtrotstudio.ghost.client.network.Packet
import com.boxtrotstudio.ghost.client.network.PlayerClient

class JoinQueuePacket : Packet<PlayerClient>() {

    override fun write(vararg args : Any) {
        val queue = args[0] as Byte;

        write(0x05.toByte())
        write(queue)
        endTCP()
    }
}
