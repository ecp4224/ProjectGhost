package com.boxtrotstudio.ghost.client.handlers

import box2dLight.ConeLight
import box2dLight.Light
import box2dLight.PointLight
import box2dLight.RayHandler
import box2dLight.p3d.P3dLight
import box2dLight.p3d.P3dPointLight
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.core.game.Entity
import com.boxtrotstudio.ghost.client.core.logic.Handler
import com.boxtrotstudio.ghost.client.handlers.scenes.LoadingScene
import com.boxtrotstudio.ghost.client.handlers.scenes.SpriteScene
import com.boxtrotstudio.ghost.client.handlers.scenes.builder.BuilderOverlayScene
import com.boxtrotstudio.ghost.client.handlers.scenes.builder.ImageSelectScene
import com.boxtrotstudio.ghost.client.utils.Timer
import java.util.*

class LightBuildHandler : Handler {
    var started = false
    private lateinit var world: SpriteScene
    var lights = ArrayList<Light>()
    var entities = ArrayList<Entity>()
    var currentDraggingLight: Light? = null
    var lastCurrentLight : Light? = null
    var currentDraggingEntity: Entity? = null
    var lastCurrentEntity: Entity? = null
    var currentPositionOffset = Vector2(0f, 0f)
    var lockLights = false
    private lateinit var scene : BuilderOverlayScene

    override fun start() {
        val loading = LoadingScene()
        Ghost.getInstance().addScene(loading)
        loading.setLoadedCallback(Runnable {
            Ghost.getInstance().addScene(ImageSelectScene(this))
            Ghost.getInstance().removeScene(loading)
        })
    }

    var escPressed = false
    override fun tick() {
        if (!started)
            return

        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE) && !escPressed) {
            scene.isVisible = !scene.isVisible
            escPressed = true
        } else if (!Gdx.input.isKeyPressed(Input.Keys.ESCAPE) && escPressed) {
            escPressed = false
        }

        if (lastCurrentLight != null) {
            if (lastCurrentLight is PointLight) {
                scene.showPointLightProperties()
            } else {
                scene.showConeLightProperties()
            }
        } else {
            scene.hideLightProperties()
        }

        scene.setVisibleEntityProperties(lastCurrentEntity != null)

        if (Gdx.input.isTouched && !lockLights && currentDraggingEntity == null) {
            val pos = Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
            Ghost.getInstance().camera.unproject(pos)
            if (currentDraggingLight == null) {
                for (light in lights) {
                    if (light.contains(pos.x, pos.y)) {
                        currentDraggingLight = light
                        lastCurrentLight = currentDraggingLight
                        currentPositionOffset.x = light.x - pos.x
                        currentPositionOffset.y = light.y - pos.y

                        currentDraggingEntity = null
                        lastCurrentEntity = null
                        break
                    }
                }

            } else if (currentDraggingLight != null) {
                val cur = currentDraggingLight as Light

                cur.setPosition(pos.x + currentPositionOffset.x, pos.y + currentPositionOffset.y)
                scene.updateInfo(cur)
            }
        } else {
            currentDraggingLight = null
        }


        if (Gdx.input.isTouched && currentDraggingLight == null) {
            val pos = Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
            Ghost.getInstance().camera.unproject(pos)

            if (currentDraggingEntity == null) {
                for (entity in entities) {
                    if (entity.contains(pos.x, pos.y) && (currentDraggingEntity == null || (currentDraggingEntity as Entity).zIndex < entity.z)) {
                        currentDraggingEntity = entity
                        lastCurrentEntity = currentDraggingEntity
                        currentPositionOffset.x = entity.x - pos.x
                        currentPositionOffset.y = entity.y - pos.y

                        lastCurrentLight = null
                    }
                }

                if (currentDraggingEntity != null)
                    scene.updateEntityInfo(currentDraggingEntity as Entity)

            } else if (currentDraggingEntity != null) {
                val cur = currentDraggingEntity as Entity

                cur.x = pos.x + currentPositionOffset.x
                cur.y = pos.y + currentPositionOffset.y

                scene.updateEntityInfo(cur)
            }
        } else {
            currentDraggingEntity = null
        }
    }

    public fun startBuilder(map: String) {
        Ghost.getInstance().clearScreen()
        world = SpriteScene()
        Ghost.getInstance().addScene(world)
        scene = BuilderOverlayScene(this)
        scene.requestOrder(-2)
        Ghost.getInstance().addScene(scene)

        val background = Entity(map, -1)

        background.zIndex = -1000

        world.addEntity(background)

        Timer({
            Gdx.app.postRunnable {
                val newTexture = Texture(Gdx.files.internal(map))
                background.texture = newTexture
            }
        }, 1000).start()
        //RayHandler.setGammaCorrection(true)
        //RayHandler.setDiffuseLight(false)
    }

    fun addPointLight() {
        val light = PointLight(Ghost.rayHandler, 128, Color.WHITE, 50f, 1280f / 2f, 720f / 2f)

        lights.add(light)

        scene.updateInfo(light)
    }

    fun addConeLight() {
        val light = ConeLight(Ghost.rayHandler, 128, Color.WHITE, 250f, 1280f / 2f, 720f / 2f, 90f, 30f)

        lights.add(light)
    }

    fun addImage(file: FileHandle) {
        val entity = Entity.fromImage(file)
        entity.x = 1280f / 2f
        entity.y = 720f / 2f

        world.addEntity(entity)
        entities.add(entity)
    }
}