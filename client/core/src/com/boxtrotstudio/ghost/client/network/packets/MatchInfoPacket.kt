package com.boxtrotstudio.ghost.client.network.packets

import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.core.game.Characters
import com.boxtrotstudio.ghost.client.network.Packet
import com.boxtrotstudio.ghost.client.network.PlayerClient

class MatchInfoPacket : Packet<PlayerClient>() {

    override fun handle() {
        val startX : Float = consume(4).asFloat()
        val startY : Float = consume(4).asFloat()
        val selfID = consume(2).asShort()
        val selfC = Characters.fromByte(consume(1).asByte())
        val allyCount = consume(4).asInt()
        val enemyCount = consume(4).asInt()

        for (i in 0 until enemyCount) {
            val stringSize = consume(4).asInt()
            val name = consume(stringSize).asString()
            val character = Characters.fromByte(consume(1).asByte())
            Ghost.enemies.put(name, character)
        }

        for (i in 0 until allyCount) {
            val stringSize = consume(4).asInt()
            val name = consume(stringSize).asString()
            val character = Characters.fromByte(consume(1).asByte())
            Ghost.allies.put(name, character)
        }

        Ghost.PLAYER_ENTITY_ID = selfID
        Ghost.selfCharacter = selfC

        if (Ghost.onMatchFound != null) {
            Ghost.onMatchFound.run(startX, startY)
        }
    }
}