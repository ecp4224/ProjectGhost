package com.boxtrotstudio.ghost.client.network.packets

import com.boxtrotstudio.ghost.client.network.Packet
import com.boxtrotstudio.ghost.client.network.PlayerClient

class ActionRequestPacket : Packet<PlayerClient>() {

    override fun write(vararg args : Any) {
        val action = args[0] as Byte
        val x = args[1] as Float
        val y = args[2] as Float

        client.sendCount++

        write(0x08.toByte())
        write(client.sendCount)
        write(action)
        write(x)
        write(y)
        endUdp()
    }
}
