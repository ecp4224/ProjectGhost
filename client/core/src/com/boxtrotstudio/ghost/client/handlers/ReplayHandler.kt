package com.boxtrotstudio.ghost.client.handlers

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.google.common.io.Files
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.core.game.Entity
import com.boxtrotstudio.ghost.client.core.game.EntityFactory
import com.boxtrotstudio.ghost.client.core.game.sprites.Mirror
import com.boxtrotstudio.ghost.client.core.game.sprites.NetworkPlayer
import com.boxtrotstudio.ghost.client.core.game.sprites.Wall
import com.boxtrotstudio.ghost.client.core.game.sprites.effects.Effect
import com.boxtrotstudio.ghost.client.core.game.timeline.EntitySpawnSnapshot
import com.boxtrotstudio.ghost.client.core.game.timeline.MatchHistory
import com.boxtrotstudio.ghost.client.core.game.timeline.TimelineCursor
import com.boxtrotstudio.ghost.client.core.logic.Handler
import com.boxtrotstudio.ghost.client.core.render.Text
import com.boxtrotstudio.ghost.client.handlers.scenes.LoadingScene
import com.boxtrotstudio.ghost.client.handlers.scenes.SpriteScene
import com.boxtrotstudio.ghost.client.utils.Global
import com.boxtrotstudio.ghost.client.utils.Vector2f
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
            loading?.setText("Loading replay..");
            loadReplay()
        })
    }

    protected fun loadReplay() {
        Thread(Runnable {
            var data = Files.toByteArray(File(Path))
            var json : String?

            var compressed = ByteArrayInputStream(data)
            var zip = GZIPInputStream(compressed)
            var result = ByteArrayOutputStream()

            zip.copyTo(result)
            json = result.toString("ASCII")

            compressed.close()
            zip.close()
            result.close()


            ReplayData = Global.GSON.fromJson(json, MatchHistory::class.java)
            cursor = ReplayData.timeline.createCursor()
            cursor?.reset()

            loaded = true

            world.isVisible = true
            if (loading != null) {
                Ghost.getInstance().removeScene(loading as LoadingScene)
            }
        }).start()
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
                    SpawnEntity(it.isPlayableEntity, it.type.toInt(), it.id, it.name, it.x, it.y, it.rotation, it.width, it.height)
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

    private fun SpawnEntity(isPlayable : Boolean, type : Int, id : Short, name : String, x : Float, y : Float, rotation : Double, width: Short, height: Short) {
        if(entities.containsKey(id)){
            var e = entities[id]
            if(e != null) {
                world.removeEntity(e)
            }
            entities.remove(id)
        }

        if(isPlayable){
            var isTeam1 = ReplayData.team1().usernames.contains(name)
            var player = NetworkPlayer(id, name)
            player.setCenter(x, y)
            player.color = if(isTeam1) allyColor else enemyColor

            world.addEntity(player)
            entities[id] = player

            var username = Text(24, Color(1f, 1f, 1f, 1f), Gdx.files.internal("fonts/INFO56_0.ttf"))

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

            entity.setOrigin(entity.width / 2f, entity.height / 2f)

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
        p.dead = isDead
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

        entity?.rotation = Math.toDegrees(rotation).toFloat()

        entity?.centerX = x
        entity?.centerY = y

        if(hasTarget){
            entity?.target?.x = target.x
            entity?.target?.y = target.y
        }

        entity?.alpha = alpha / 255f
        if(entity != null && entity.alpha < (100f / 255f) && entity is NetworkPlayer){
            entity.alpha = (100f / 255f)
        }
    }
}

