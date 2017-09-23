package com.boxtrotstudio.ghost.client.core.game.maps

import com.boxtrotstudio.ghost.client.core.game.SpriteEntity
import com.boxtrotstudio.ghost.client.handlers.scenes.SpriteScene

class RoofTop : MapCreator {
    override fun construct(world: SpriteScene) {
        val background = SpriteEntity("maps/scene2.png", -1)
        val background2 = SpriteEntity("sprites/scene2_NONlit.png", -1)
        val background3 = SpriteEntity("sprites/scene2_glass_NONlit.png", -1)

        background3.x = 2f
        background.y = 33f

        background.zIndex = -1000
        background2.zIndex = -1000
        background3.zIndex = -1000

        world.addEntity(background)
        world.addEntity(background2)
        world.addEntity(background3)
    }

    override fun name(): String {
        return "RoofTop"
    }

}