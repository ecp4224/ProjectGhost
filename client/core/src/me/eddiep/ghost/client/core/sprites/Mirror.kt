package me.eddiep.ghost.client.core.sprites

import me.eddiep.ghost.client.Ghost
import me.eddiep.ghost.client.core.Entity
import me.eddiep.ghost.client.core.physics.Face
import me.eddiep.ghost.client.core.physics.Hitbox
import me.eddiep.ghost.client.core.physics.PhysicsEntity
import me.eddiep.ghost.client.core.physics.PolygonHitbox
import me.eddiep.ghost.client.utils.Vector2f
import me.eddiep.ghost.client.utils.VectorUtils

class Mirror(id: Short) : Entity("sprites/wall.png", id), PhysicsEntity {
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
            val ex = e.centerX
            val ey = e.centerY
            val oldPoint = Vector2f(ex - (e.velocity.x * 1.5f), ey - (e.velocity.y * 1.5f))
            val endPoint = Vector2f(ex + (e.velocity.x * 100), ey + (e.velocity.y * 100))

            var closestFace: Face? = null
            var closestPoint = Vector2f.ZERO
            var distance = 9999999999.0

            for (face in _hitbox.polygon.faces) {
                val pointOfIntersection = VectorUtils.pointOfIntersection(oldPoint, endPoint, face.pointA, face.pointB)
                if (pointOfIntersection == Vector2f.ZERO || pointOfIntersection == null)
                    continue

                val d = Vector2f.distance(pointOfIntersection, oldPoint)
                if (closestFace == null) {
                    closestFace = face
                    closestPoint = pointOfIntersection
                    distance = d
                } else if (d < distance) {
                    closestFace = face
                    closestPoint = pointOfIntersection
                    distance = d
                }
            }

            if (closestFace == null)
                return@addPhysicsEntity

            e.onMirrorHit(closestFace, closestPoint)
        }, _hitbox)
    }
}
