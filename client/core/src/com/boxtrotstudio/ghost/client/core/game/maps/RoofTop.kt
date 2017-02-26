package com.boxtrotstudio.ghost.client.core.game.maps

import com.boxtrotstudio.ghost.client.core.game.SpriteEntity
import com.boxtrotstudio.ghost.client.handlers.scenes.SpriteScene

class RoofTop : MapCreator {
    override fun construct(world: SpriteScene) {
        val background = SpriteEntity("maps/scene2.png", -1)

        background.zIndex = -1000

        world.addEntity(background)
    }

    override fun name(): String {
        return "RoofTop"
    }

}