package com.boxtrotstudio.ghost.client.network.packets

import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.network.Packet
import com.boxtrotstudio.ghost.client.network.PlayerClient

class PingPongPacket : Packet<PlayerClient>() {

    override fun write(vararg args: Any?) {
        val toSend = System.currentTimeMillis()

        write(0x09.toByte())
        write(toSend)
        write(ByteArray(24))
        endUdp()
    }

    override fun handle() {
        val pingStartTime = consume(8).asLong()

        val timeTook = System.currentTimeMillis() - pingStartTime
        Ghost.latency = timeTook
    }
}