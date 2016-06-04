package com.boxtrotstudio.ghost.client.network.packets

import com.boxtrotstudio.ghost.client.network.Packet
import com.boxtrotstudio.ghost.client.network.PlayerClient

class MatchStatusPacket : Packet<PlayerClient>() {

    override fun handle() {
        val state = consume(1).asBoolean()
        val reasonLength = consume(4).asInt()
        val reason = consume(reasonLength).asString()

        client.game.updateStatus(state, reason)
    }
}
