package me.eddiep.ghost.client.core.sprites.effects

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.utils.TimeUtils
import me.eddiep.ghost.client.Ghost
import me.eddiep.ghost.client.core.Blend
import me.eddiep.ghost.client.core.Entity
import me.eddiep.ghost.client.core.Logical
import me.eddiep.ghost.client.utils.Global

class ChargeEffect : Effect {
    val RANGE: Double = 0.785398163

    override fun begin(duration: Int, size: Int, x: Float, y: Float, rotation: Double) {
        val min = rotation - RANGE
        val max = rotation + RANGE
        val count = Global.RANDOM.nextInt(100) + 100
        val sprites: Array<ChargeSprite?> = arrayOfNulls(count)

        for (i in 0..count-1) {
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

                    for (i in cursor..cursor + toSpawn-1) {
                        val sprite = sprites[i] ?: continue;
                        Ghost.getInstance().addEntity(sprite)
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

class ChargeSprite : Entity {
    var duration: Float = 0f
    var startX: Float = 0f
    var startY: Float = 0f
    var startTime: Long = 0;
    val cX: Float
    val cY: Float


    constructor(cX: Float, cY: Float) : super("sprites/ball.png", 0) {
        duration = (Global.RANDOM.nextInt(1000 - 100) + 100).toFloat()
        setBlend(Blend(GL20.GL_SRC_ALPHA, GL20.GL_ONE));
        this.cX = cX
        this.cY = cY
    }

    override fun onLoad() {
        super.onLoad()

        setScale(Global.RANDOM.nextFloat() * (0.2f - 0.15f) + 0.15f)
        color = Color(25 / 255f, 158 / 255f, 208 / 255f, 1f)
    }

    override fun tick() {
        super.tick()

        if (startTime == 0L) {
            startTime = TimeUtils.millis()
            startX = centerX
            startY = centerY
        }

        val newX = Entity.ease(startX, cX, duration, (TimeUtils.millis() - startTime).toFloat())
        val newY = Entity.ease(startY, cY, duration, (TimeUtils.millis() - startTime).toFloat())
        val newAlpha = Entity.ease(1f, 0.5f, duration, (TimeUtils.millis() - startTime).toFloat())

        setCenter(newX, newY)
        setAlpha(newAlpha)

        if (newX == cX && newY == cY) {
            Ghost.getInstance().removeEntity(this)
        }
    }
}

