package me.eddiep.ghost.client.network.packets

import me.eddiep.ghost.client.Ghost
import me.eddiep.ghost.client.core.game.TemporaryStats
import me.eddiep.ghost.client.handlers.scenes.StatsScene
import me.eddiep.ghost.client.network.Packet
import me.eddiep.ghost.client.network.PlayerClient

class MatchEndPacket : Packet<PlayerClient>() {

    override fun handle() {
        val isWinner = consume(1).asBoolean()
        val matchId = consume(8).asLong()
        val chunkSize = consume(4).asInt()
        val stats = consume(chunkSize).`as`(TemporaryStats::class.java)

        val statScreen = StatsScene(stats.get(TemporaryStats.SHOTS_FIRED).toInt(), stats.get(TemporaryStats.SHOTS_HIT).toInt(), stats.get(TemporaryStats.HAT_TRICK) == 1L, stats.get(TemporaryStats.ITEM_USAGE).toInt())
        statScreen.requestOrder(-5)
        Ghost.getInstance().addScene(statScreen)

        client.game.endMatch()
    }
}
