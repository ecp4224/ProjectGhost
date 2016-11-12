package com.boxtrotstudio.ghost.client.network.packets

import com.boxtrotstudio.ghost.client.network.Packet
import com.boxtrotstudio.ghost.client.network.PlayerClient

class SpawnEntityPacket : Packet<PlayerClient>() {

    override fun handle() {
        val type : Short = consume(2).asShort()
        val id : Short = consume(2).asShort()

        val nameLength = consume(4).asInt()
        val name : String = consume(nameLength).asString()

        val x : Float = consume(4).asFloat()
        val y : Float = consume(4).asFloat()

        val angle : Double = consume(8).asDouble()

        val width = consume(2).asShort()
        val height = consume(2).asShort()

        val hasLighting = consume(1).asBoolean()

        client.game.spawn(type, id, name, x, y, angle, width, height, hasLighting)
    }
}

