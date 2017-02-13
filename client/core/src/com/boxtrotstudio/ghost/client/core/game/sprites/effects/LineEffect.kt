package com.boxtrotstudio.ghost.client.core.game.sprites.effects

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.TimeUtils
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.core.game.SpriteEntity
import com.boxtrotstudio.ghost.client.core.logic.Logical
import com.boxtrotstudio.ghost.client.core.physics.Face
import com.boxtrotstudio.ghost.client.core.render.Blend
import com.boxtrotstudio.ghost.client.handlers.scenes.SpriteScene
import com.boxtrotstudio.ghost.client.utils.Global
import com.boxtrotstudio.ghost.client.utils.Vector2f
import org.jetbrains.annotations.NotNull

class LineEffect : Effect {
    override fun begin(duration: Int, size: Int, x: Float, y: Float, rotation: Double, world: SpriteScene) {
        val count = Global.rand(400, 600)
        val sprites: Array<LineSprite?> = arrayOfNulls(count)

        for (i in 0..count-1) {
            val range = (size/1000.0)-0.01
            val angleToAdd = Global.RANDOM.nextDouble()*(range - -range)+ -range

            val sprite = LineSprite(rotation + angleToAdd, duration)
            sprite.setCenter(x, y)
            sprite.setRotation(Math.toDegrees(rotation + angleToAdd).toFloat())
            sprite.setBlend(Blend.ADDITIVE);

            sprites[i] = sprite
        }

        var cursor = 0
        var lastSpawn = 0L
        var nextSpawn = 0L
        var startTime = TimeUtils.millis()

        Ghost.getInstance().addLogical(object: Logical {

            override fun tick() {
                if (TimeUtils.millis() - startTime >= duration) {
                    Ghost.getInstance().removeLogical(this)
                    return
                }
                if (TimeUtils.millis() - lastSpawn > nextSpawn) {
                    var toSpawn = Global.rand(40, 80)
                    toSpawn = Math.min(count - cursor, toSpawn)

                    for (i in cursor..cursor + toSpawn-1) {
                        val sprite = sprites[i] ?: continue;
                        world.addEntity(sprite)
                    }

                    cursor += toSpawn
                    lastSpawn = TimeUtils.millis()
                    nextSpawn = (Global.rand(10, 100)).toLong()
                }
            }

            override fun dispose() {

            }
        })
    }
}

class LineSprite(val rotation: Double, val baseDuration: Int) : SpriteEntity("sprites/ball.png", 0) {
    val duration = Global.rand(baseDuration, (baseDuration * 1.5).toInt()).toFloat()
    val speed = Global.rand(30, 70)
    var start = 0f

    override fun onLoad() {
        super.onLoad()

        setZ(1000)
        setScale(Global.RANDOM.nextFloat()*(0.35f - 0.2f)+0.2f)
        color = Color(194 / 255f, 19 / 255f, 19 / 255f, 1f)

        velocity = Vector2f(Math.cos(rotation).toFloat() * speed, Math.sin(rotation).toFloat() * speed)
        target = Vector2f(9999f, 9999f)
    }

    override fun tick() {
        super.tick()

        if (start == 0f) {
            start = TimeUtils.millis().toFloat()
        }

        val newAlpha = ease(1f, 0f, duration, (TimeUtils.millis() - start))

        setAlpha(newAlpha)

        if (newAlpha == 0f) {
            parentScene.removeEntity(this)
            return
        }

        Ghost.PHYSICS.checkEntity(this)
    }

    var didHit = false
    override fun onMirrorHit(closestFace: Face, closestPoint: Vector2f) {
        super.onMirrorHit(closestFace, closestPoint)

        if (!didHit) {
            didHit = true
            velocity.scale(speed.toFloat())
        } else {
            parentScene.removeEntity(this)
        }
    }
}
