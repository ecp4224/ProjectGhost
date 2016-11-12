package com.boxtrotstudio.ghost.client.core.game.maps

import box2dLight.RayHandler
import box2dLight.p3d.P3dDirectionalLight
import box2dLight.p3d.P3dPointLight
import com.badlogic.gdx.graphics.Color
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.core.game.Entity
import com.boxtrotstudio.ghost.client.handlers.scenes.SpriteScene

class Test : MapCreator {
    override fun construct(world: SpriteScene) {
        val background = Entity("maps/test.png", -1)

        background.zIndex = -1000

        world.addEntity(background)

        RayHandler.setGammaCorrection(true)
        //RayHandler.useDiffuseLight(true)
        Ghost.rayHandler.setAmbientLight(0.4f, 0.4f, 0.4f, 1.0f)


        /*val light = P3dDirectionalLight(Ghost.rayHandler, 128 * 4, Color(1f, 1f, 1f, 0.4f), 90f + 90f + 90f)
        light.setHeight(2f)

        val light2 = P3dPointLight(Ghost.rayHandler, 128, Color.GREEN, 500f, 512f, 360f)
        light2.setHeight(5f)*/

        //ConeLight(world.rayHandler, 128, Color.BLUE, 300f, 200f, 400f, 45f, 30f)

        //val light = PointLight(world.rayHandler, 256, Color(1f, 1f, 1f, 0.4f), 700f, 512f, 360f)
    }

    override fun name(): String {
        return "Test Map"
    }

}
