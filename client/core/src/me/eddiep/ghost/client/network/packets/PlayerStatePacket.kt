package me.eddiep.ghost.client.network.packets

import me.eddiep.ghost.client.Ghost
import me.eddiep.ghost.client.core.sprites.NetworkPlayer
import me.eddiep.ghost.client.network.Packet
import me.eddiep.ghost.client.network.PlayerClient

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
