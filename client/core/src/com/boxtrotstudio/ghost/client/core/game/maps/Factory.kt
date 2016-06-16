package com.boxtrotstudio.ghost.client.core.game.maps

import com.boxtrotstudio.ghost.client.core.game.Entity
import com.boxtrotstudio.ghost.client.handlers.scenes.SpriteScene

class Factory : MapCreator {
    override fun construct(world: SpriteScene) {
        val background = Entity("maps/factory_background.png", -1)
        val foreground = Entity("maps/factory_foreground.png", -1)

        background.zIndex = -1000
        foreground.zIndex = 1000

        world.addEntity(background)
        world.addEntity(foreground)

        /*Ghost.lights.add(LightCreator {
            ConeLight(world.rayHandler, 128, Color.WHITE, 200f, 230f, 520f, 315f, 40f)
        })
        Ghost.lights.add(LightCreator {
            ConeLight(world.rayHandler, 128, Color.WHITE, 200f, 740f, 195f, 135f, 40f)
        })
        Ghost.lights.add(LightCreator {
            ConeLight(world.rayHandler, 128, Color.WHITE, 200f, 230f, 195f, 45f, 40f)
        })
        Ghost.lights.add(LightCreator {
            ConeLight(world.rayHandler, 128, Color.WHITE, 200f, 740f, 520f, 225f, 40f)
        })*/
    }

    override fun name(): String {
        return "factory"
    }

}
