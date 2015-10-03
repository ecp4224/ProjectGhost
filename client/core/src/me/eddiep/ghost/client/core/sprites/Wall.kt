package me.eddiep.ghost.client.core.sprites

import me.eddiep.ghost.client.Ghost
import me.eddiep.ghost.client.core.Entity
import me.eddiep.ghost.client.core.physics.Hitbox
import me.eddiep.ghost.client.core.physics.PhysicsEntity
import me.eddiep.ghost.client.core.physics.PolygonHitbox
import me.eddiep.ghost.client.utils.PRunnable
import me.eddiep.ghost.client.utils.Vector2f

class Wall(id: Short) : Entity("sprites/wall.png", id), PhysicsEntity {
    val _hitbox: Hitbox by lazy {
        val x1 = x
        val x2 = x + width
        val y1 = y
        val y2 = y + height

        PolygonHitbox("WALL", Vector2f(x1, y1), Vector2f(x1, y2), Vector2f(x2, y2), Vector2f(x2, y1))
    }

    override fun getHitbox(): Hitbox? {
        return _hitbox
    }

    override fun onLoad() {
        super.onLoad()

        Ghost.PHYSICS.addPhysicsEntity({
            e ->
            Ghost.getInstance().removeEntity(e)
        }, _hitbox)
    }
}
