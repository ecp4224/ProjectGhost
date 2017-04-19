package com.boxtrotstudio.ghost.client.network.packets

import com.boxtrotstudio.ghost.client.network.Packet
import com.boxtrotstudio.ghost.client.network.PlayerClient

class UdpSessionPacket : Packet<PlayerClient>() {

    override fun write(vararg args : Any) {
        val session : String = args[0] as String;

        write(0x00.toByte())
        write(session.length.toShort())
        write(session)
        endUdp()
    }
}