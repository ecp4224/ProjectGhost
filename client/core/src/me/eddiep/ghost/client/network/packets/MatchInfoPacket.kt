package me.eddiep.ghost.client.network.packets

import me.eddiep.ghost.client.Ghost
import me.eddiep.ghost.client.core.game.Characters
import me.eddiep.ghost.client.network.Packet
import me.eddiep.ghost.client.network.PlayerClient

class MatchInfoPacket : Packet<PlayerClient>() {

    override fun handle() {
        val startX : Float = consume(4).asFloat()
        val startY : Float = consume(4).asFloat()
        val selfID = consume(2).asShort()
        val selfC = Characters.fromByte(consume(1).asByte())
        val allyCount = consume(4).asInt()
        val enemyCount = consume(4).asInt()

        for (i in 0..enemyCount-1) {
            val stringSize = consume(4).asInt()
            val name = consume(stringSize).asString()
            val character = Characters.fromByte(consume(1).asByte())
            Ghost.enemies.put(name, character)
        }

        for (i in 0..allyCount-1) {
            val stringSize = consume(4).asInt()
            val name = consume(stringSize).asString()
            val character = Characters.fromByte(consume(1).asByte())
            Ghost.allies.put(name, character)
        }

        Ghost.PLAYER_ENTITY_ID = selfID;
        Ghost.selfCharacter = selfC;

        if (Ghost.onMatchFound != null) {
            Ghost.onMatchFound.run(startX, startY)
        }
    }
}