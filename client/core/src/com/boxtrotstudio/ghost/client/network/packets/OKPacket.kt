package com.boxtrotstudio.ghost.client.network.packets

import com.boxtrotstudio.ghost.client.network.Packet
import com.boxtrotstudio.ghost.client.network.PlayerClient

class OKPacket() : Packet<PlayerClient>() {

    override fun handle() {
        val isOk : Boolean = consume(1).asBoolean()

        client.setOk(isOk)
    }
}
