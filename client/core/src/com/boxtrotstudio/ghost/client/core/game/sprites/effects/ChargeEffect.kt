package com.boxtrotstudio.ghost.client.core.game.sprites.effects

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.TimeUtils
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.core.game.SpriteEntity
import com.boxtrotstudio.ghost.client.core.logic.Logical
import com.boxtrotstudio.ghost.client.core.render.Blend
import com.boxtrotstudio.ghost.client.handlers.scenes.SpriteScene
import com.boxtrotstudio.ghost.client.utils.Global

class ChargeEffect : Effect {
    val RANGE: Double = 0.785398163

    override fun begin(duration: Int, size: Int, x: Float, y: Float, rotation: Double, world: SpriteScene) {
        val min = rotation - RANGE
        val max = rotation + RANGE
        val count = Global.RANDOM.nextInt(100) + 100
        val sprites: Array<ChargeSprite?> = arrayOfNulls(count)

        for (i in 0 until count) {
            val location = Global.RANDOM.nextDouble()*(max-min)+min
            val trueSize = Global.RANDOM.nextInt(size - (size/2))+size

            val spawnX = (Math.cos(location)*trueSize) + x
            val spawnY = (Math.sin(location)*trueSize) + y

            val sprite = ChargeSprite(x, y)
            sprite.setCenter(spawnX.toFloat(), spawnY.toFloat())
            sprite.rotation = Math.toDegrees(rotation).toFloat()

            sprites[i] = sprite
        }

        var cursor = 0

        var lastSpawn = 0L
        var nextSpawn = 0L
        val startTime = TimeUtils.millis()

        Ghost.getInstance().addLogical(object: Logical {

            override fun tick() {
                if (TimeUtils.millis() - startTime >= duration) {
                    Ghost.getInstance().removeLogical(this)
                    return
                }
                if (TimeUtils.millis() - lastSpawn > nextSpawn) {
                    var toSpawn = Global.RANDOM.nextInt(20) + 20
                    toSpawn = Math.min(count - cursor, toSpawn)

                    for (i in cursor until cursor + toSpawn) {
                        val sprite = sprites[i] ?: continue
                        world.addEntity(sprite)
                    }

                    cursor += toSpawn
                    lastSpawn = TimeUtils.millis()
                    nextSpawn = (Global.RANDOM.nextInt(100-10) + 10).toLong()
                }
            }

            override fun dispose() {

            }
        })
    }
}

class ChargeSprite : SpriteEntity {
    var duration: Float = 0f
    var startX: Float = 0f
    var startY: Float = 0f
    var startTime: Long = 0
    val cX: Float
    val cY: Float


    constructor(cX: Float, cY: Float) : super("sprites/ball.png", 0) {
        duration = (Global.RANDOM.nextInt(600 - 100) + 100).toFloat()
        setBlend(Blend.ADDITIVE)
        this.cX = cX
        this.cY = cY
    }

    override fun onLoad() {
        super.onLoad()

        val min = 0.05f
        val max = 0.1f
        setScale(Global.RANDOM.nextFloat()*(max - min)+min)
        color = Color(25 / 255f, 158 / 255f, 208 / 255f, 1f)
    }

    override fun tick() {
        super.tick()

        if (startTime == 0L) {
            startTime = TimeUtils.millis()
            startX = centerX
            startY = centerY
        }

        val newX = ease(startX, cX, duration, (TimeUtils.millis() - startTime).toFloat())
        val newY = ease(startY, cY, duration, (TimeUtils.millis() - startTime).toFloat())
        val newAlpha = ease(1f, 0.5f, duration, (TimeUtils.millis() - startTime).toFloat())

        setCenter(newX, newY)
        alpha = newAlpha

        if (newX == cX && newY == cY) {
            parentScene.removeEntity(this)
        }
    }
}

