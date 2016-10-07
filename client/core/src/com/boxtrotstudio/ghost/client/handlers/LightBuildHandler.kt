package com.boxtrotstudio.ghost.client.handlers

import box2dLight.ConeLight
import box2dLight.Light
import box2dLight.PointLight
import box2dLight.RayHandler
import box2dLight.p3d.P3dLight
import box2dLight.p3d.P3dPointLight
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.core.game.Entity
import com.boxtrotstudio.ghost.client.core.logic.Handler
import com.boxtrotstudio.ghost.client.handlers.scenes.LoadingScene
import com.boxtrotstudio.ghost.client.handlers.scenes.SpriteScene
import com.boxtrotstudio.ghost.client.handlers.scenes.builder.BuilderOverlayScene
import com.boxtrotstudio.ghost.client.handlers.scenes.builder.ImageSelectScene
import java.util.*

class LightBuildHandler : Handler {
    var started = false
    private lateinit var world: SpriteScene
    var lights = ArrayList<Light>()
    var currentDragging: Light? = null
    var lastCurrentLight : Light? = null
    var currentPositionOffset = Vector2(0f, 0f)
    private lateinit var scene : BuilderOverlayScene

    override fun start() {
        val loading = LoadingScene()
        Ghost.getInstance().addScene(loading)
        loading.setLoadedCallback(Runnable {
            Ghost.getInstance().addScene(ImageSelectScene(this))
            Ghost.getInstance().removeScene(loading)
        })
    }


    override fun tick() {
        if (!started)
            return

        if (Gdx.input.isTouched) {
            if (currentDragging == null) {
                for (light in lights) {
                    if (light.contains(Gdx.input.x.toFloat(), 720f - Gdx.input.y)) {
                        currentDragging = light
                        lastCurrentLight = currentDragging;
                        currentPositionOffset.x = light.x - Gdx.input.x
                        currentPositionOffset.y = light.y - (720f - Gdx.input.y)
                        break
                    }
                }

            } else if (currentDragging != null) {
                val cur = currentDragging as Light

                cur.setPosition(Gdx.input.x + currentPositionOffset.x, (720f - Gdx.input.y) + currentPositionOffset.y)
                scene.updateInfo(cur)
            }
        } else {
            currentDragging = null
        }
    }

    public fun startBuilder(map: String) {
        started = true
        Ghost.getInstance().clearScreen()
        world = SpriteScene()
        Ghost.getInstance().addScene(world)
        scene = BuilderOverlayScene(this)
        scene.requestOrder(-2)
        Ghost.getInstance().addScene(scene)

        val background = Entity(map, -1)

        background.zIndex = -1000

        world.addEntity(background)

        //RayHandler.setGammaCorrection(true)
        //RayHandler.setDiffuseLight(false)
    }

    fun addPointLight() {
        val light = PointLight(Ghost.rayHandler, 128, Color.WHITE, 50f, 100f, 100f)

        lights.add(light)

        scene.updateInfo(light)
    }

    fun addConeLight() {
        val light = ConeLight(Ghost.rayHandler, 128, Color.WHITE, 250f, 100f, 100f, 90f, 30f)

        lights.add(light)
    }
}