package com.boxtrotstudio.ghost.client.network.packets

import com.boxtrotstudio.ghost.client.network.Packet
import com.boxtrotstudio.ghost.client.network.PlayerClient

class StatsUpdatePacket : Packet<PlayerClient>() {

    override fun handle() {
        val id = consume(4).asString()
        val value = consume(8).asDouble()

        when (id) {
            "mspd" -> client.game.player1.speedStat = value
            "frte" -> client.game.player1.fireRateStat = value
        }
    }
}
