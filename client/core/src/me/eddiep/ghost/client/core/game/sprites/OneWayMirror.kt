package me.eddiep.ghost.client.core.game.sprites

import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import me.eddiep.ghost.client.Ghost
import me.eddiep.ghost.client.core.game.Entity
import me.eddiep.ghost.client.core.physics.Hitbox
import me.eddiep.ghost.client.core.physics.PhysicsEntity
import me.eddiep.ghost.client.core.physics.PolygonHitbox
import me.eddiep.ghost.client.utils.Direction
import me.eddiep.ghost.client.utils.Vector2f

class OneWayMirror(id: Short) : Entity("sprites/oneway.png", id), PhysicsEntity {
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
            val facing = Direction.fromDegrees(rotation.toDouble() + 90.0)

            val normalVel = e.velocity.cloneVector()
            normalVel.normalise()

            val velocityDirection = normalVel.getDirection()

            if (facing != velocityDirection) {
                parentScene.removeEntity(e)
            }

        }, _hitbox)

        val wallBodyDef = BodyDef()

        val pos = Vector3(centerX, (y + (height / 2f)), 0f)

        val wallBody = Ghost.getInstance().world.createBody(wallBodyDef)

        val wallBox = PolygonShape()
        wallBox.setAsBox(width / 2f, height / 2f)

        wallBody.createFixture(wallBox, 0.0f)

        wallBox.dispose()

        wallBody.setTransform(pos.x, pos.y, Math.toRadians(rotation.toDouble()).toFloat())
    }
}
