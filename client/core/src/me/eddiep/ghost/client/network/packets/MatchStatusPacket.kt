package me.eddiep.ghost.client.network.packets

import me.eddiep.ghost.client.network.Packet
import me.eddiep.ghost.client.network.PlayerClient

class MatchStatusPacket : Packet<PlayerClient>() {

    override fun handle() {
        val state = consume(1).asBoolean()
        val reasonLength = consume(4).asInt()
        val reason = consume(reasonLength).asString()

        client.game.updateStatus(state, reason)
    }
}
