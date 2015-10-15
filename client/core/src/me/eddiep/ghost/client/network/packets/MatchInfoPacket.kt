package me.eddiep.ghost.client.network.packets

import me.eddiep.ghost.client.Ghost
import me.eddiep.ghost.client.network.Packet
import me.eddiep.ghost.client.network.PlayerClient

class MatchInfoPacket : Packet<PlayerClient>() {

    override fun handle() {
        val startX : Float = consume(4).asFloat()
        val startY : Float = consume(4).asFloat()

        if (Ghost.onMatchFound != null) {
            Ghost.onMatchFound.run(startX, startY)
        }
    }
}