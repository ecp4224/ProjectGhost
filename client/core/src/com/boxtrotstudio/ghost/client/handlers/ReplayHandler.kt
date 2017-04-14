package com.boxtrotstudio.ghost.client.handlers

import box2dLight.ConeLight
import box2dLight.PointLight
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.core.game.*
import com.boxtrotstudio.ghost.client.core.game.events.StandardEvent
import com.boxtrotstudio.ghost.client.core.game.maps.MapCreator
import com.boxtrotstudio.ghost.client.core.game.sprites.Mirror
import com.boxtrotstudio.ghost.client.core.game.sprites.NetworkPlayer
import com.boxtrotstudio.ghost.client.core.game.sprites.Wall
import com.boxtrotstudio.ghost.client.core.game.timeline.EntitySpawnSnapshot
import com.boxtrotstudio.ghost.client.core.game.timeline.MatchHistory
import com.boxtrotstudio.ghost.client.core.game.timeline.TimelineCursor
import com.boxtrotstudio.ghost.client.core.logic.Handler
import com.boxtrotstudio.ghost.client.core.render.Text
import com.boxtrotstudio.ghost.client.handlers.scenes.LoadingScene
import com.boxtrotstudio.ghost.client.handlers.scenes.SpriteScene
import com.boxtrotstudio.ghost.client.utils.Global
import com.boxtrotstudio.ghost.client.utils.Vector2f
import com.google.common.io.Files
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import java.util.zip.GZIPInputStream


open class ReplayHandler(public var Path: String?) : Handler {

    var entities : HashMap<Short, Entity> = HashMap<Short, Entity>()

    lateinit var ReplayData : MatchHistory
    private var loaded : Boolean = false
    private var paused : Boolean = false
    protected var cursor : TimelineCursor? = null
    var loading: LoadingScene? = null
    lateinit var world: SpriteScene
    private var lastUpdate : Long = 0

    val allyColor : Color = Color(0f, 0.341176471f, 0.7725490196f, 1f)
    val enemyColor : Color = Color(0.7725490196f, 0f, 0f, 1f)

    override fun start() {
        loading = LoadingScene()
        Ghost.getInstance().addScene(loading as LoadingScene)

        world = SpriteScene()
        Ghost.getInstance().addScene(world)
        world.isVisible = false

        loading?.setLoadedCallback(Runnable {
            //loading?.setText("Loading replay..");
            loadReplay()
        })
    }

    protected fun loadReplay() {
        Thread(Runnable {
            Ghost.PHYSICS.clear()
            Ghost.getInstance().clearBodies()
            
            if (Ghost.rayHandler != null)
                Ghost.rayHandler.removeAll()

            val data = Files.toByteArray(File(Path))
            val json : String?

            val compressed = ByteArrayInputStream(data)
            val zip = GZIPInputStream(compressed)
            val result = ByteArrayOutputStream()

            zip.copyTo(result)
            json = result.toString("ASCII")

            compressed.close()
            zip.close()
            result.close()


            ReplayData = Global.GSON.fromJson(json, MatchHistory::class.java)
            cursor = ReplayData.timeline.createCursor()
            cursor?.reset()

            val mapName = ReplayData.map.name
            MapCreator.MAPS
                    .filter { it.name() == mapName }
                    .forEach { it.construct(world) }

            spawnLights()

            loaded = true

            world.isVisible = true
            if (loading != null) {
                Ghost.getInstance().removeScene(loading as LoadingScene)
            }
        }).start()
    }

    fun spawnLights() {
        for (info in ReplayData.map.startingLocations) {
            if (info.id.toInt() == -1) {
                val x = info.x
                val y = info.y
                var radius = 250f
                var intensity = 1f

                if (info.hasExtra("radius")) {
                    radius = java.lang.Float.parseFloat(info.getExtra("radius"))
                }
                if (info.hasExtra("intensity")) {
                    intensity = java.lang.Float.parseFloat(info.getExtra("intensity"))
                }

                val javaColor = java.awt.Color(info.color[0] / 255f, info.color[1] / 255f, info.color[2] / 255f, intensity)
                val color = javaColor.red shl 24 or
                            (javaColor.green shl 16) or
                            (javaColor.blue shl 8) or
                            javaColor.alpha

                var shadows = false
                if (info.hasExtra("shadows")) {
                    shadows = info.getExtra("shadows").equals("true", ignoreCase = true)
                }
                if (info.hasExtra("cone")) {
                    if (info.getExtra("cone").equals("true", ignoreCase = true)) {
                        var directionDegrees = 270f
                        var coneDegrees = 30f
                        radius = 350f

                        if (info.hasExtra("directionDegrees")) {
                            directionDegrees = java.lang.Float.parseFloat(info.getExtra("directionDegrees"))
                        }

                        if (info.hasExtra("coneDegrees")) {
                            coneDegrees = java.lang.Float.parseFloat(info.getExtra("coneDegrees"))
                        }

                        Gdx.app.postRunnable {
                            val c = Color(color)

                            Gdx.app.postRunnable {
                                val rayHandler = Ghost.rayHandler
                                val light = ConeLight(rayHandler, 128, c, radius, x, y, directionDegrees, coneDegrees)
                                light.isCastShadows = shadows
                            }
                        }
                    }
                } else {
                    Gdx.app.postRunnable {
                        val c = Color(color)

                        Gdx.app.postRunnable {
                            val rayHandler = Ghost.rayHandler
                            val light = PointLight(rayHandler, 128, c, radius, x, y)
                            light.isCastShadows = shadows
                        }
                    }
                }
            }
        }
    }

    override fun tick() {
       if(!loaded || (CheckKeyboard() || paused)) return

       if ((cursor?.isPresent) == false) {
           showUpdate()
           cursor?.tick()
       }
    }

    private fun showUpdate(){
        var snapshot = cursor?.get()
        if (snapshot == null)
            return

        if(snapshot.entitySpawnSnapshots != null){
            snapshot.entitySpawnSnapshots.forEach {
                if(it.isParticle){
                    SpawnParticle(it)
                }else{
                    SpawnEntity(it.isPlayableEntity, it.type.toInt(), it.id, it.name, it.x, it.y, it.rotation, it.width, it.height, it.hasLighting())
                }
            }
        }

        if(snapshot.entityDespawnSnapshots != null){
            snapshot.entityDespawnSnapshots.forEach {
                var entityToRemove = entities[it.id]

                if(entities.containsKey(it.id)){
                    if(entityToRemove != null){
                        world.removeEntity(entityToRemove)
                        entities.remove(it.id)
                    }
                }
            }
        }

        if(snapshot.entitySnapshots != null){
            snapshot.entitySnapshots.forEach {
                if(it != null){
                    UpdateEntity(it.id, it.x, it.y, it.velX, it.velY, it.alpha, it.rotation, it.hasTarget(), Vector2f(it.targetX, it.targetY))
                }
            }
        }

        if(snapshot.playableChanges != null){
            snapshot.playableChanges.forEach {
                UpdatePlayable(it.id, it.lives, it.isDead, it.isFrozen)
            }
        }

        if(entities.count() != snapshot.entitySnapshots.count()){
            var toRemove : MutableList<Short> = arrayListOf()
            entities.keys.forEach {
                if(!(entities[it] is Wall) && !(entities[it] is Mirror)){
                    var found = false
                    for(entity in snapshot.entitySnapshots){
                        if(entity != null && entity.id == it){
                            found = true
                            break
                        }
                    }
                    if(!found)
                        toRemove.add(it)
                }
            }
            toRemove.forEach {
                var entityToRemove = entities[it]
                if(entityToRemove != null){
                    world.removeEntity(entityToRemove)
                }
                entities.remove(it)
            }
        }

        if (snapshot.events != null) {
            snapshot.events.forEach {
                launchEvent(it.eventId, it.causeId, it.direction)
            }
        }
    }

    private fun launchEvent(eventID: Short, causeID: Short, direction: Double) {
        for (event in StandardEvent.values()) {
            if (event.id == eventID) {
                val cause = findEntity(causeID)
                event.trigger(cause, direction, world)
                break
            }
        }
    }

    fun findEntity(id: Short): SpriteEntity {
        return entities[id] as SpriteEntity
    }

    private fun SpawnParticle(event : EntitySpawnSnapshot){
        var type = event.type
        var data = event.name.split(':')
        var duration = data[0].toInt()
        var size = data[1].toInt()
        var rotation = data[2].toDouble()

        com.boxtrotstudio.ghost.client.core.game.sprites.effects.Effect.EFFECTS[type.toInt()].begin(duration, size, event.x, event.y, rotation, world)
    }

    private fun CheckKeyboard() : Boolean {
        //TODO: implement Sharp2D ButtonChecker
        return false
    }

    private fun SpawnEntity(isPlayable : Boolean, type : Int, id : Short, name : String, x : Float, y : Float, rotation : Double, width: Short, height: Short, hasLighting: Boolean) {
        if(entities.containsKey(id)){
            var e = entities[id]
            if(e != null) {
                world.removeEntity(e)
            }
            entities.remove(id)
        }

        if(isPlayable){
            //var isTeam1 = ReplayData.team1().usernames.contains(name)

            val character = Characters.fromByte(ReplayData.teamFor(name).getWeaponFor(name))

            val player =
                    if (character == Characters.DOT) NetworkPlayer(id, "sprites/ball.png")
                    else CharacterCreator.createNetworkPlayer(character, "DEFAULT", id)

            //var player = NetworkPlayer(id, "sprites/ball.png")
            player.setCenter(x, y)
            if (player.isDot)
                player.color = if (ReplayData.teamFor(name).teamNumber == 1) allyColor else enemyColor

            world.addEntity(player)
            entities[id] = player

            val username = Text(24, Color(1f, 1f, 1f, 1f), Gdx.files.internal("fonts/TitilliumWeb-Regular.ttf"))

            username.y = player.centerY + 32f
            username.x = player.centerX
            username.text = name
            player.attach(username)
            world.addEntity(username)
        }else{
            var entity : Entity? = EntityFactory.createEntity(type.toShort(), id, x, y, width.toFloat(), height.toFloat(), rotation.toFloat(), name)

            if(entity == null){
                print("An invalid entity ID was sent from the server!")
                print("Skipping...")
                return
            }

            if (entity is SpriteEntity)
                entity.setOrigin(entity.width / 2f, entity.height / 2f)

            /*val entity: Entity? =
                    if (type.toInt() != -3)
                        EntityFactory.createEntity(type.toShort(), id, x, y, width.toFloat(), height.toFloat(), rotation.toFloat(), name)
                    else {
                        if (Ghost.ASSETS.isLoaded(name))
                            SpriteEntity(name, id)
                        else
                            SpriteEntity("sprites/$name", id)
                    }


            if (entity == null) {
                System.err.println("An invalid entity ID was sent by the server! (ID: $type)");
                return;
            }

            if (type != -3 && type != 93 && entity is SpriteEntity) {
                entity.setOrigin(entity.width / 2f, entity.height / 2f)
                entity.z = -1
            } else {
                entity.x = x
                entity.y = y
                entity.setSize(width.toFloat(), height.toFloat())
                entity.rotation = rotation.toFloat()

                entity.z = -1
                entity.setHasLighting(hasLighting)
            }*/

            //entity.setHasLighting(hasLighting)

            world.addEntity(entity)
            entities[id] = entity
        }
    }

    private fun UpdatePlayable(id : Short, lifeCount : Byte, isDead : Boolean, isFrozen : Boolean){
        var p : NetworkPlayer
        if(!entities.containsKey(id)) return

        p = entities[id] as NetworkPlayer

        if(p == null) return

        p.lives = lifeCount
        //p.dead = isDead
        p.frozen = isFrozen
    }

    private fun UpdateEntity(entityID : Short, x : Float, y : Float, xvel : Float, yvel : Float, alpha : Int, rotation: Double, hasTarget : Boolean, target : Vector2f){
        var entity : Entity?

        if(entities.containsKey(entityID)){
            entity = entities[entityID]
        } else {
            System.out.println("Couldn't find " + entityID)
            return
        }

        if (entity == null)
            return

        entity.rotation = Math.toDegrees(rotation).toFloat()

        entity.centerX = x
        entity.centerY = y

        //Set velocity for animations
        if (entity.velocity == null) {
            entity.velocity = Vector2f(xvel, yvel)
        } else {
            entity.velocity.x = xvel
            entity.velocity.y = yvel
        }

        if(hasTarget){
            entity.target?.x = target.x
            entity.target?.y = target.y
        }

        entity.alpha = alpha / 255f
        if(entity.alpha < (50f / 255f) && entity is NetworkPlayer){
            entity.alpha = (50f / 255f)
        }
    }
}

