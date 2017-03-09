package com.boxtrotstudio.ghost.client.network.packets

import com.boxtrotstudio.ghost.client.network.Packet
import com.boxtrotstudio.ghost.client.network.PlayerClient

class RemoveTextPacket : Packet<PlayerClient>() {

    override fun handle() {
        val id = consume(8).asLong()

        client.game.removeText(id)
    }
}