package com.boxtrotstudio.ghost.client.network.packets

import com.boxtrotstudio.ghost.client.network.Packet
import com.boxtrotstudio.ghost.client.network.PlayerClient

class SetNamePacket : Packet<PlayerClient>() {

    override fun write(vararg args : Any) {
        var username = args[0] as String

        for (i in username.length..254) {
            username += " "
        }

        write(0x41.toByte())
        write(username)
        endTCP()
    }
}
