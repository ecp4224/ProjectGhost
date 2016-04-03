package com.boxtrotstudio.ghost.client.network.packets

import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.network.Packet
import com.boxtrotstudio.ghost.client.network.PlayerClient


class UpdateSessionPacket : Packet<PlayerClient>(){
    override fun handle(){
        val newSession = consume(consume(4).asInt()).asString()

        Ghost.Session = newSession
    }
}
