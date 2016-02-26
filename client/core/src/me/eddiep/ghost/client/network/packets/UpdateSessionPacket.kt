package me.eddiep.ghost.client.network.packets

import me.eddiep.ghost.client.Ghost
import me.eddiep.ghost.client.network.Packet
import me.eddiep.ghost.client.network.PlayerClient


class UpdateSessionPacket : Packet<PlayerClient>(){
    override fun handle(){
        val newSession = consume(consume(4).asInt()).asString()

        Ghost.Session = newSession
    }
}
