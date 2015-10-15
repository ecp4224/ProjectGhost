package me.eddiep.ghost.client.network.packets

import me.eddiep.ghost.client.core.sprites.effects.Effect
import me.eddiep.ghost.client.network.Packet
import me.eddiep.ghost.client.network.PlayerClient

class SpawnEffectPacket : Packet<PlayerClient>() {

    override fun handle() {
        val effectType = consume(1).asByte()
        val duration = consume(4).asInt()
        val size = consume(4).asInt()
        val x = consume(4).asFloat()
        val y = consume(4).asFloat()

        val rotation = consume(8).asDouble()

        Effect.EFFECTS[effectType.toInt()].begin(duration, size, x, y, rotation)
    }
}
