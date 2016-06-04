package com.boxtrotstudio.ghost.client.network.packets

import com.boxtrotstudio.ghost.client.core.game.sprites.effects.Effect
import com.boxtrotstudio.ghost.client.network.Packet
import com.boxtrotstudio.ghost.client.network.PlayerClient

class SpawnEffectPacket : Packet<PlayerClient>() {

    override fun handle() {
        val effectType = consume(1).asByte()
        val duration = consume(4).asInt()
        val size = consume(4).asInt()
        val x = consume(4).asFloat()
        val y = consume(4).asFloat()

        val rotation = consume(8).asDouble()

        com.boxtrotstudio.ghost.client.core.game.sprites.effects.Effect.EFFECTS[effectType.toInt()].begin(duration, size, x, y, rotation, client.game.world)
    }
}
