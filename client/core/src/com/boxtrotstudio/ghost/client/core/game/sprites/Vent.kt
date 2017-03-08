package com.boxtrotstudio.ghost.client.core.game.sprites

import com.boxtrotstudio.ghost.client.core.game.SimpleAnimatedSprite

class Vent(short: Short) : SimpleAnimatedSprite("sprites/Airvent_animsheet.png", short, 3, 20) {

    override fun onLoad() {
        super.onLoad()

        animation.play()
    }
}