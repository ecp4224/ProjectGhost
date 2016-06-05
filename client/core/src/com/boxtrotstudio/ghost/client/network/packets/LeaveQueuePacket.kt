package com.boxtrotstudio.ghost.client.network.packets

import com.boxtrotstudio.ghost.client.network.Packet
import com.boxtrotstudio.ghost.client.network.PlayerClient


class LeaveQueuePacket : Packet<PlayerClient>() {

    override fun write(vararg args : Any) {
        write(0x20.toByte())
        endTCP()
    }
}