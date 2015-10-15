package me.eddiep.ghost.client.network.packets

import me.eddiep.ghost.client.Ghost
import me.eddiep.ghost.client.core.sprites.NetworkPlayer
import me.eddiep.ghost.client.core.sprites.effects.OrbitEffect
import me.eddiep.ghost.client.network.Packet
import me.eddiep.ghost.client.network.PlayerClient

class ItemActivatedPacket : Packet<PlayerClient>() {

    override fun handle() {
        val id = consume(2).asShort()
        val owner = consume(2).asShort()

        if (id == 12.toShort()) {
            val e = client.game.findEntity(owner);

            if (e is NetworkPlayer) {
                val effect = OrbitEffect(e)
                effect.begin()
            }
        }

        System.out.println("Item $id was activated by $owner!");
    }
}
