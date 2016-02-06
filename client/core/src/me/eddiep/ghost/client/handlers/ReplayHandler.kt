package me.eddiep.ghost.client.handlers

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.google.common.io.Files
import me.eddiep.ghost.client.Ghost
import me.eddiep.ghost.client.core.game.Entity
import me.eddiep.ghost.client.core.game.EntityFactory
import me.eddiep.ghost.client.core.game.sprites.Mirror
import me.eddiep.ghost.client.core.game.sprites.NetworkPlayer
import me.eddiep.ghost.client.core.game.sprites.Wall
import me.eddiep.ghost.client.core.game.sprites.effects.Effect
import me.eddiep.ghost.client.core.game.timeline.EntitySpawnSnapshot
import me.eddiep.ghost.client.core.game.timeline.MatchHistory
import me.eddiep.ghost.client.core.game.timeline.TimelineCursor
import me.eddiep.ghost.client.core.logic.Handler
import me.eddiep.ghost.client.core.render.Text
import me.eddiep.ghost.client.core.render.scene.impl.LoadingScene
import me.eddiep.ghost.client.core.render.scene.impl.SpriteScene
import me.eddiep.ghost.client.utils.Global
import me.eddiep.ghost.client.utils.Vector2f
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import java.util.zip.GZIPInputStream


class ReplayHandler(public var Path: String?) : Handler {

    private var entities : HashMap<Short, Entity> = HashMap<Short, Entity>()

    lateinit var ReplayData : MatchHistory
    private var loaded : Boolean = false
    private var paused : Boolean = false
    lateinit private var cursor : TimelineCursor
    lateinit var loading: LoadingScene
    lateinit var world: SpriteScene
    private var lastUpdate : Long = 0

    val allyColor : Color = Color(0f, 0.341176471f, 0.7725490196f, 1f)
    val enemyColor : Color = Color(0.7725490196f, 0f, 0f, 1f)

    override fun start() {
        loading = LoadingScene()
        Ghost.getInstance().addScene(loading)

        world = SpriteScene()
        Ghost.getInstance().addScene(world)
        world.isVisible = false

        loading.setLoadedCallback(Runnable {
            loading.setText("Loading replay..");

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

                loaded = true

                world.isVisible = true
                Ghost.getInstance().removeScene(loading)
            }).start()
        })
    }

    override fun tick() {
       if(!loaded || (CheckKeyboard() || paused)) return

       //TODO: implement ShowUpdate

       cursor.tick()
    }

    private fun showUpdate(){
        var snapshot = cursor.get()

        if(snapshot.entitySpawnSnapshots != null){
            snapshot.entitySpawnSnapshots.forEach {
                if(it.isParticle){
                    SpawnParticle(it)
                }else{
                    SpawnEntity(it.isPlayableEntity, it.type.toInt(), it.id, it.name, it.x, it.y, it.rotation)
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
        var duration = data.get(0).toInt()
        var size = data.get(0).toInt()
        var rotation = data.get(2).toDouble()

        Effect.EFFECTS[type.toInt()].begin(duration, size, event.x, event.y, rotation, world)
    }

    private fun CheckKeyboard() : Boolean {
        //TODO: implement Sharp2D ButtonChecker
        return false
    }

    private fun SpawnEntity(isPlayable : Boolean, type : Int, id : Short, name : String, x : Float, y : Float, rotation : Double){
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
            player.oColor = if(isTeam1) allyColor else enemyColor

            world.addEntity(player)
            entities.set(id, player) //

            var username = Text(18, Color.WHITE, Gdx.files.internal("fonts/INFO56_0.ttf"))

            username.y = player.centerY - 32f
            username.x = player.centerX
            world.addEntity(username)
        }else{
            var entity : Entity? = EntityFactory.createEntity(type.toShort(), id, x, y)

            if(entity == null){
                print("An invalid entity ID was sent from the server!")
                print("Skipping...")
                return
            }
            entity.rotation = rotation.toFloat()
            world.addEntity(entity)
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
        } else return

        entity?.rotation = rotation.toFloat()

        entity?.centerX = x
        entity?.centerY = y

        if(hasTarget){
            entity?.target?.x = target.x
            entity?.target?.y = target.y
        }

        entity?.alpha = alpha / 255f
        if(entity != null && entity.alpha > (100f / 255f) && entity is NetworkPlayer){
            entity.alpha = (100f / 255f)
        }
    }
}

