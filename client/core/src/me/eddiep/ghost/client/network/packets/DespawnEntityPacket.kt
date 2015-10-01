package me.eddiep.ghost.client.network.packets

import me.eddiep.ghost.client.network.Packet
import me.eddiep.ghost.client.network.PlayerClient

class DespawnEntityPacket : Packet<PlayerClient>() {

    override fun handle() {
        val id : Short = consume(2).asShort()

        client.game.despawn(id)
    }
}

