package com.boxtrotstudio.ghost.client.core.game.sprites

import com.boxtrotstudio.ghost.client.core.game.SpriteEntity
import com.boxtrotstudio.ghost.client.utils.Direction
import com.boxtrotstudio.ghost.client.utils.Global
import com.boxtrotstudio.ghost.client.utils.Vector2f

class Leaf(val direction : Direction, val minBounds : Vector2f, val maxBounds : Vector2f) : SpriteEntity("sprites/leaf.png", 0) {
    var sinMulti = 0.005
    var sinAdd = 0
    var sinTick = 0
    var funMult = 5
    var rotationSpeed = 0
    var truePos = 0f
    var speed = 0
    override fun onLoad() {
        super.onLoad()

        reset()
    }

    override fun tick() {
        rotation += rotationSpeed

        if (Global.RANDOM.nextDouble() < 0.01) {
            rotationSpeed = Global.RANDOM.nextInt(4) + 2
        }

        sinTick++

        when (direction) {
            Direction.LEFT -> {
                x -= speed
                y = ((Math.sin((sinMulti * sinTick.toDouble()) + sinAdd.toDouble()) * funMult) + truePos).toFloat()

                if (x <= minBounds.x) {
                    reset()
                }
            }
            Direction.RIGHT -> {
                x += speed
                y = ((Math.sin((sinMulti * sinTick.toDouble()) + sinAdd.toDouble()) * funMult) + truePos).toFloat()

                if (x >= maxBounds.x) {
                    reset()
                }
            }
            Direction.UP -> {
                y -= speed
                x = ((Math.sin((sinMulti * sinTick.toDouble()) + sinAdd.toDouble()) * funMult) + truePos).toFloat()

                if (y <= minBounds.y) {
                    reset()
                }
            }
            Direction.DOWN -> {
                y += speed
                x = ((Math.sin((sinMulti * sinTick.toDouble()) + sinAdd.toDouble()) * funMult) + truePos).toFloat()

                if (y >= maxBounds.y) {
                    reset()
                }
            }
            else -> {
                x += speed
                y = ((Math.sin((sinMulti * sinTick.toDouble()) + sinAdd.toDouble()) * funMult) + truePos).toFloat()

                if (x >= maxBounds.x) {
                    reset()
                }
            }
        }
    }

    private fun reset() {
        if (direction == Direction.UP || direction == Direction.DOWN) {
            x = Global.rand(minBounds.x.toInt(), maxBounds.x.toInt()).toFloat()
        }

        if (direction == Direction.LEFT || direction == Direction.RIGHT) {
            y = Global.rand(minBounds.y.toInt(), maxBounds.y.toInt()).toFloat()
        }

        when (direction) {
            Direction.UP -> y = minBounds.y
            Direction.DOWN -> y = maxBounds.y
            Direction.LEFT -> x = maxBounds.x
            Direction.RIGHT -> x = minBounds.x
            else -> x = minBounds.x
        }

        sinMulti = Global.RANDOM.nextDouble() / 10.0
        sinAdd = Global.RANDOM.nextInt()
        sinTick = Global.RANDOM.nextInt()
        rotationSpeed = Global.RANDOM.nextInt(3) + 2
        speed = Global.RANDOM.nextInt(10 - 6) + 6
        funMult = Global.RANDOM.nextInt(speed + 10) + 3

        if (direction == Direction.UP || direction == Direction.DOWN)
            truePos = centerX
        else
            truePos = centerY
    }
}
