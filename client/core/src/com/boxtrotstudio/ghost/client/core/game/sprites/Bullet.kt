package com.boxtrotstudio.ghost.client.core.game.sprites

import com.badlogic.gdx.graphics.Color
import com.boxtrotstudio.ghost.client.core.game.Entity

class Bullet(id: Short) : Entity("sprites/ball.png", id) {

    override fun onLoad() {
        super.onLoad()

        setScale(0.25f)
        color = Color(29 / 255f, 52 / 255f, 215 / 255f, 1f)
    }
}
