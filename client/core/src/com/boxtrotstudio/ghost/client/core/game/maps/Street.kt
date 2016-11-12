package com.boxtrotstudio.ghost.client.core.game.maps

import com.boxtrotstudio.ghost.client.core.game.Entity
import com.boxtrotstudio.ghost.client.handlers.scenes.SpriteScene

class Street : MapCreator {
    override fun construct(world: SpriteScene) {
        val background = Entity("maps/Scene_1.png", -1)

        background.zIndex = -1000

        world.addEntity(background)
    }

    override fun name(): String {
        return "Street"
    }

}