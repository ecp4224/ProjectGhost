package me.eddiep.ghost.client.network.packets

import me.eddiep.ghost.client.Ghost
import me.eddiep.ghost.client.network.Packet
import me.eddiep.ghost.client.network.PlayerClient

class MatchInfoPacket : Packet<PlayerClient>() {

    override fun handle() {
        val startX : Float = consume(4).asFloat()
        val startY : Float = consume(4).asFloat()
        val enemyCount = consume(4).asInt()

        for (i in 0..enemyCount-1) {
            val stringSize = consume(4).asInt()
            val name = consume(stringSize).asString()
            System.out.println(name) //TODO Do something with this
        }

        if (Ghost.onMatchFound != null) {
            Ghost.onMatchFound.run(startX, startY)
        }
    }
}