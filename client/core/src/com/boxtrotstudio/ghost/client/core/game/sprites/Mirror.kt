package com.boxtrotstudio.ghost.client.core.game.sprites

import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.core.game.Entity
import com.boxtrotstudio.ghost.client.core.physics.Face
import com.boxtrotstudio.ghost.client.core.physics.Hitbox
import com.boxtrotstudio.ghost.client.core.physics.PhysicsEntity
import com.boxtrotstudio.ghost.client.core.physics.PolygonHitbox
import com.boxtrotstudio.ghost.client.utils.Vector2f
import com.boxtrotstudio.ghost.client.utils.VectorUtils

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
