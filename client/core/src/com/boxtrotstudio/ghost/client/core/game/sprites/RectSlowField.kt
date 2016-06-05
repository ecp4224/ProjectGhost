package com.boxtrotstudio.ghost.client.core.game.sprites

import com.boxtrotstudio.ghost.client.core.game.Entity
import com.boxtrotstudio.ghost.client.utils.Direction
import com.boxtrotstudio.ghost.client.utils.Global
import com.boxtrotstudio.ghost.client.utils.Vector2f

class RectSlowField(val id : Short) : Entity("sprites/leaf.png", id) {

    override fun onLoad() {
        super.onLoad()

        isVisible = false

        var direction = Direction.RIGHT
        if (width > height) {
            when (Global.RANDOM.nextBoolean()) {
                true -> direction = Direction.RIGHT
                false -> direction = Direction.LEFT
            }
        } else if (height > width) {
            when (Global.RANDOM.nextBoolean()) {
                true -> direction = Direction.UP
                false -> direction = Direction.DOWN
            }
        } else {
            when (Global.RANDOM.nextBoolean()) {
                true -> direction = Direction.RIGHT
                false -> direction = Direction.LEFT
            }
        }

        var minBounds = Vector2f(0f, 0f)
        var maxBounds = Vector2f(0f, 0f)

        minBounds.x = centerX - (width / 2f)
        minBounds.y = y

        maxBounds.x = centerX + (width / 2f)
        maxBounds.y = y + height

        val count = Global.rand(10, 20)
        for (i in 0..count) {
            parentScene.addEntity(Leaf(direction, minBounds, maxBounds))
        }
    }
}
