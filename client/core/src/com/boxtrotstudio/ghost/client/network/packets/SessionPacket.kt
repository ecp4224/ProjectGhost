package com.boxtrotstudio.ghost.client.network.packets

import com.boxtrotstudio.ghost.client.network.Packet
import com.boxtrotstudio.ghost.client.network.PlayerClient
import com.boxtrotstudio.ghost.client.network.Stream

class SessionPacket : Packet<PlayerClient>() {

    override fun write(vararg args : Any) {
        val session : String = args[0] as String;

        write(0x00.toByte())
        write(session.length.toShort())
        write(session)

        if (args.size == 2) {
            val stream = args[1] as Stream
            write(stream.level.toByte())
        }

        endTCP()
    }
}