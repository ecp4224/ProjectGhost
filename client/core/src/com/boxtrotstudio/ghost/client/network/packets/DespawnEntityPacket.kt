package com.boxtrotstudio.ghost.client.network.packets

import com.boxtrotstudio.ghost.client.network.Packet
import com.boxtrotstudio.ghost.client.network.PlayerClient

class DespawnEntityPacket : Packet<PlayerClient>() {

    override fun handle() {
        val id : Short = consume(2).asShort()

        client.game.despawn(id)
    }
}

