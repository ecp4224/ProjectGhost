package me.eddiep.ghost.client.network.packets

import me.eddiep.ghost.client.network.Packet
import me.eddiep.ghost.client.network.PlayerClient

class MatchEndPacket : Packet<PlayerClient>() {

    override fun handle() {
        val isWinner = consume(1).asBoolean()
        val matchId = consume(8).asLong()

        client.game.endMatch()
    }
}
