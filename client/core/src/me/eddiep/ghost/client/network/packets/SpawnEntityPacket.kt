package me.eddiep.ghost.client.network.packets

import me.eddiep.ghost.client.Ghost
import me.eddiep.ghost.client.network.Packet
import me.eddiep.ghost.client.network.PlayerClient

class SpawnEntityPacket : Packet<PlayerClient>() {

    override fun handle() {
        val type : Short = consume(2).asShort()
        val id : Short = consume(2).asShort()

        val nameLength : Byte = consume(1).asByte()
        val name : String = consume(nameLength.toInt()).asString()

        val x : Float = consume(4).asFloat()
        val y : Float = consume(4).asFloat()

        val angle : Double = consume(8).asDouble()

        client.game.spawn(type, id, name, x, y, angle)
    }
}

