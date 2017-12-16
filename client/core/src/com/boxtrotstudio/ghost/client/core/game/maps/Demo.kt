package com.boxtrotstudio.ghost.client.core.game.maps

import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.core.game.SpriteEntity
import com.boxtrotstudio.ghost.client.handlers.scenes.SpriteScene

class Demo : MapCreator {
    override fun construct(world: SpriteScene) {
        val background = SpriteEntity("maps/demo_background.png", -1)

        background.zIndex = -1000

        world.addEntity(background)
        //RayHandler.useDiffuseLight(true)
        Ghost.rayHandler.setAmbientLight(0.4f, 0.4f, 0.4f, 1.0f)


        /*val light = P3dDirectionalLight(Ghost.rayHandler, 128 * 4, Color(0.6f, 0.6f, 0.6f, 0.4f), 90f + 90f + 90f)
        light.setHeight(2f)*/
    }

    override fun name(): String {
        return "Demo_Test"
    }

}
