package com.boxtrotstudio.ghost.client.handlers

import box2dLight.ConeLight
import box2dLight.Light
import box2dLight.PointLight
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.core.game.Entity
import com.boxtrotstudio.ghost.client.core.game.EntityFactory
import com.boxtrotstudio.ghost.client.core.game.SpriteEntity
import com.boxtrotstudio.ghost.client.core.game.sprites.Wall
import com.boxtrotstudio.ghost.client.core.logic.Handler
import com.boxtrotstudio.ghost.client.handlers.scenes.LoadingScene
import com.boxtrotstudio.ghost.client.handlers.scenes.SpriteScene
import com.boxtrotstudio.ghost.client.handlers.scenes.builder.BuilderOverlayScene
import com.boxtrotstudio.ghost.client.handlers.scenes.builder.ImageSelectScene
import com.boxtrotstudio.ghost.client.utils.Global
import com.boxtrotstudio.ghost.client.utils.PFunction
import com.boxtrotstudio.ghost.client.utils.Timer
import com.boxtrotstudio.ghost.client.utils.WorldMap
import java.util.*

class LightBuildHandler : Handler {
    var started = false
    private lateinit var world: SpriteScene
    var lights = ArrayList<Light>()
    var entities = ArrayList<SpriteEntity>()
    var currentDraggingLight: Light? = null
    var lastCurrentLight : Light? = null
    var currentDraggingEntity: SpriteEntity? = null
    var lastCurrentEntity: SpriteEntity? = null
    var currentPositionOffset = Vector2(0f, 0f)
    var lockLights = false
    var backgroundPath = ""
    var lastSaveTime = 0L
    var lastSaveLocation : FileHandle? = null
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

        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            scene.openSaveDialog()
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
                    if (entity.contains(pos.x, pos.y) && (currentDraggingEntity == null || (currentDraggingEntity as SpriteEntity).zIndex < entity.z)) {
                        currentDraggingEntity = entity
                        lastCurrentEntity = currentDraggingEntity
                        currentPositionOffset.x = entity.x - pos.x
                        currentPositionOffset.y = entity.y - pos.y

                        lastCurrentLight = null
                    }
                }

                if (currentDraggingEntity != null)
                    scene.updateEntityInfo(currentDraggingEntity as SpriteEntity)

            } else if (currentDraggingEntity != null) {
                val cur = currentDraggingEntity as SpriteEntity

                cur.x = pos.x + currentPositionOffset.x
                cur.y = pos.y + currentPositionOffset.y

                scene.updateEntityInfo(cur)
            }
        } else {
            currentDraggingEntity = null
        }

        if (lastSaveLocation != null) {
            if (System.currentTimeMillis() - lastSaveTime >= 5000) {
                saveTo(lastSaveLocation as FileHandle)
            }
        }
    }

    public fun saveTo(file: FileHandle) {
        var map = WorldMap()

        val ambiant = map.AmbiantColor()

        ambiant.red = (Ghost.rayHandler.ambientLight.r * 255).toInt()
        ambiant.green = (Ghost.rayHandler.ambientLight.g * 255).toInt()
        ambiant.blue = (Ghost.rayHandler.ambientLight.b * 255).toInt()

        map.setAmbiantColor(ambiant)

        map.ambiantPower = Ghost.rayHandler.ambientLight.a

        map.name = "Unknown"
        map.backgroundTexture = backgroundPath

        val list = ArrayList<WorldMap.EntityLocation>()

        for (light in lights) {
            val entity = map.EntityLocation()

            entity.id = -1
            entity.x = light.x
            entity.y = light.y
            entity.color = intArrayOf((light.color.r * 255).toInt(), (light.color.g * 255).toInt(), (light.color.b * 255).toInt())

            entity.addExtra("radius", light.distance.toString())
            entity.addExtra("intensity", light.color.a.toString())

            if (light is ConeLight) {
                val cone = light// as ConeLight //Cast not needed

                entity.addExtra("cone", "true")
                entity.addExtra("directionDegrees", cone.direction.toString())
                entity.addExtra("coneDegrees", cone.coneDegree.toString())
            }

            list.add(entity)
        }

        for (e in entities) {
            val entity = map.EntityLocation()

            if (e is Wall) {
                entity.id = 85
                entity.x = e.centerX
                entity.y = e.centerY
                entity.rotation = Math.toRadians(e.rotation.toDouble())
                entity.width = (e.width * e.scaleX).toShort()
                entity.height = (e.height * e.scaleY).toShort()

                list.add(entity)
            } else {
                entity.id = -3
                entity.x = e.x
                entity.y = e.y
                entity.rotation = e.rotation.toDouble()
                entity.width = (e.width * e.scaleX).toShort()
                entity.height = (e.height * e.scaleY).toShort()

                entity.addExtra("z", e.z.toString())
                entity.addExtra("lighting", e.hasLighting().toString())
                entity.addExtra("name", e.path)

                list.add(entity)
            }
        }

        val array = list.toTypedArray()

        map.setLocations(array)

        val json = Global.GSON.toJson(map)

        file.writeString(json, false)

        lastSaveLocation = file
        lastSaveTime = System.currentTimeMillis()
    }

    public fun loadFromFile(file: FileHandle) {
        val json = file.readString()

        val map = Global.GSON.fromJson(json, WorldMap::class.java)

        backgroundPath = map.backgroundTexture
        if (!backgroundPath.trim().equals("") && !backgroundPath.equals("null")) {
            val entity = SpriteEntity(backgroundPath, -1)
            entity.zIndex = -1000
            world.addEntity(entity)

            Timer(PFunction {
                Gdx.app.postRunnable {
                    val newTexture = Texture(Gdx.files.internal(backgroundPath))
                    entity.texture = newTexture
                }
                return@PFunction false
            }, 1000L).start()
        }


        Gdx.app.postRunnable {
            Ghost.rayHandler.ambientLight.r = (map.ambiantColor[0] / 255f)
            Ghost.rayHandler.ambientLight.g = (map.ambiantColor[1] / 255f)
            Ghost.rayHandler.ambientLight.b = (map.ambiantColor[2] / 255f)
            Ghost.rayHandler.ambientLight.a = (map.ambiantPower)
        }

        try {
            for (location in map.startingLocations) {
                if (location.id.toInt() == -1) {
                    val x = location.x
                    val y = location.y
                    var radius = 250f
                    var intensity = 1f

                    if (location.hasExtra("radius")) {
                        radius = java.lang.Float.parseFloat(location.getExtra("radius"))
                    }
                    if (location.hasExtra("intensity")) {
                        intensity = java.lang.Float.parseFloat(location.getExtra("intensity"))
                    }

                    if (location.hasExtra("cone")) {
                        var directionDegrees = 270f
                        var coneDegrees = 30f

                        if (location.hasExtra("directionDegrees")) {
                            directionDegrees = java.lang.Float.parseFloat(location.getExtra("directionDegrees"))
                        }

                        if (location.hasExtra("coneDegrees")) {
                            coneDegrees = java.lang.Float.parseFloat(location.getExtra("coneDegrees"))
                        }

                        Gdx.app.postRunnable {
                            val light = ConeLight(Ghost.rayHandler, 128, Color(location.color[0] / 255f,
                                    location.color[1] / 255f, location.color[2] / 255f, intensity),
                                    radius, x, y, directionDegrees, coneDegrees)
                            lights.add(light)
                        }
                    } else {
                        Gdx.app.postRunnable {
                            val light = PointLight(Ghost.rayHandler, 128, Color(location.color[0] / 255f,
                                    location.color[1] / 255f, location.color[2] / 255f, intensity), radius, x, y)

                            lights.add(light)
                        }
                    }
                } else if (location.id.toInt() == -3) {
                    val path = location.getExtra("name")
                    val z = location.getExtra("z").toDouble().toInt()
                    val lighting = location.getExtra("lighting").equals("true")

                    val entity = if (Ghost.ASSETS.isLoaded(path)) SpriteEntity(path, -1) else SpriteEntity("sprites/$path", -1)
                    entity.x = location.x
                    entity.y = location.y
                    entity.setSize(location.width.toFloat(), location.height.toFloat())
                    entity.rotation = location.rotation.toFloat()

                    entity.zIndex = z
                    entity.setHasLighting(lighting)

                    world.addEntity(entity)
                    entities.add(entity)
                } else {
                    val entity: Entity? = EntityFactory.createEntity(location.id, -1, location.x, location.y,
                            location.width.toFloat(), location.height.toFloat(), location.rotation.toFloat(), "NA")

                    if (entity == null || entity !is SpriteEntity) {
                        System.err.println("An invalid entity ID was sent by the server! (ID: $location.id)")
                        return
                    }

                    entity.setOrigin(entity.width / 2f, entity.height / 2f)

                    world.addEntity(entity)
                    entities.add(entity)
                }
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    public fun startBuilder(map: String) {
        Ghost.getInstance().clearScreen()
        world = SpriteScene()
        Ghost.getInstance().addScene(world)
        scene = BuilderOverlayScene(this)
        scene.requestOrder(-2)
        Ghost.getInstance().addScene(scene)

        if (map.endsWith("json")) {
            loadFromFile(Gdx.files.internal(map))
            return
        } else {
            backgroundPath = map

            val background = SpriteEntity(map, -1)

            background.zIndex = -1000

            world.addEntity(background)

            Timer(PFunction {
                Gdx.app.postRunnable {
                    val newTexture = Texture(Gdx.files.internal(map))
                    background.texture = newTexture
                }

                return@PFunction false
            }, 1000).start()
        }
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
        val entity = SpriteEntity.fromImage(file)
        entity.x = 1280f / 2f
        entity.y = 720f / 2f

        world.addEntity(entity)
        entities.add(entity)
    }

    fun addWall() {
        val entity = Wall(-1)
        entity.x = 1280f / 2f
        entity.y = 720f / 2f

        world.addEntity(entity)
        entities.add(entity)
    }
}