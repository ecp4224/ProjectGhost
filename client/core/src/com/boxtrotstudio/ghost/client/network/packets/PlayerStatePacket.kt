package com.boxtrotstudio.ghost.client.network.packets

import com.boxtrotstudio.ghost.client.core.game.sprites.NetworkPlayer
import com.boxtrotstudio.ghost.client.network.Packet
import com.boxtrotstudio.ghost.client.network.PlayerClient

class PlayerStatePacket : Packet<PlayerClient>() {

    override fun handle() {
        val id = consume(2).asShort()
        val lives = consume(1).asByte()
        val isDead = consume(1).asBoolean()
        val isFrozen = consume(1).asBoolean()
        val isInvincible = consume(1).asBoolean()

        val e = client.game.findEntity(id)

        if (e is NetworkPlayer) {
            val player : NetworkPlayer = e

            player.lives = lives
            player.dead = isDead
            player.frozen = isFrozen
        }
    }
}
