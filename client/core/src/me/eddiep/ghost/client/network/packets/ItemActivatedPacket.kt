package me.eddiep.ghost.client.network.packets

import me.eddiep.ghost.client.core.game.sprites.NetworkPlayer
import me.eddiep.ghost.client.core.game.sprites.effects.OrbitEffect
import me.eddiep.ghost.client.network.Packet
import me.eddiep.ghost.client.network.PlayerClient

class ItemActivatedPacket : Packet<PlayerClient>() {

    override fun handle() {
        val id = consume(2).asShort()
        val owner = consume(2).asShort()

        val e = client.game.findEntity(owner);

        if (id == 12.toShort()) {
            if (e is NetworkPlayer) {
                val effect = OrbitEffect(e)
                effect.begin()
            }
        }

        /*if (e is NetworkPlayer) {
            if (e.alpha > 0) {
                val item = Item.getItem(id)
                val entity = item.createEntity(-5)

                entity.setCenter(e.centerX, e.centerY)
                entity.alpha = 0f
                entity.zIndex = 1
                entity.velocity = Vector2f(0f, 0.5f)
                e.attach(entity)


                entity.fadeOutAndDespawn(1100)
                entity.scale(0.0625f)
                Ghost.getInstance().addEntity(entity)
            }
        }*/

        System.out.println("Item $id was activated by $owner!");
    }
}
