package com.boxtrotstudio.ghost.client.network.packets

import com.boxtrotstudio.ghost.client.core.game.sprites.NetworkPlayer
import com.boxtrotstudio.ghost.client.network.Packet
import com.boxtrotstudio.ghost.client.network.PlayerClient

class ItemDeactivatedPacket : Packet<PlayerClient>() {

    override fun handle() {
        val id = consume(2).asShort()
        val owner = consume(2).asShort()

        if (id == 12.toShort()) {
            val e = client.game.findEntity(owner)

            if (e is NetworkPlayer) {
                e.orbits[0].end()
            }
        }

        System.out.println("Item $id was deactivated for $owner!")
    }
}
