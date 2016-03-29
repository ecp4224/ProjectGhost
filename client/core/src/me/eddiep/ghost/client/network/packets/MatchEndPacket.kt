package me.eddiep.ghost.client.network.packets

import me.eddiep.ghost.client.core.game.TemporaryStats
import me.eddiep.ghost.client.network.Packet
import me.eddiep.ghost.client.network.PlayerClient

class MatchEndPacket : Packet<PlayerClient>() {

    override fun handle() {
        val isWinner = consume(1).asBoolean()
        val matchId = consume(8).asLong()
        val chunkSize = consume(4).asInt()
        val stats = consume(chunkSize).`as`(TemporaryStats::class.java)

        client.game.endMatch()
    }
}
