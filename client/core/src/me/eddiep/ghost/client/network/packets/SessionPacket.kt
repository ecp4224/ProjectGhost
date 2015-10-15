package me.eddiep.ghost.client.network.packets

import me.eddiep.ghost.client.network.Packet
import me.eddiep.ghost.client.network.PlayerClient

class SessionPacket : Packet<PlayerClient>() {

    override fun write(vararg args : Any) {
        val session : String = args[0] as String;

        write(0x00.toByte())
        write(session)
        endTCP()
    }
}