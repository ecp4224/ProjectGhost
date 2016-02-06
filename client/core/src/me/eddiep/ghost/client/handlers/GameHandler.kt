package me.eddiep.ghost.client.handlers

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import me.eddiep.ghost.client.Ghost
import me.eddiep.ghost.client.core.game.Entity
import me.eddiep.ghost.client.core.game.EntityFactory
import me.eddiep.ghost.client.core.game.sprites.InputEntity
import me.eddiep.ghost.client.core.game.sprites.NetworkPlayer
import me.eddiep.ghost.client.core.logic.Handler
import me.eddiep.ghost.client.core.render.Text
import me.eddiep.ghost.client.core.render.scene.Scene
import me.eddiep.ghost.client.handlers.scenes.LoadingScene
import me.eddiep.ghost.client.handlers.scenes.SpriteScene
import me.eddiep.ghost.client.network.PlayerClient
import me.eddiep.ghost.client.network.packets.SessionPacket
import me.eddiep.ghost.client.utils.P2Runnable
import me.eddiep.ghost.client.utils.Vector2f
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeoutException

class GameHandler(val IP : String, val Session : String) : Handler {
    var ambiantColor: Color = Color(1f, 1f, 1f, 1f)
    var ambiantPower : Float = 1f
    var statusText : Text? = null

    lateinit var player1 : InputEntity
    public lateinit var world : SpriteScene
    lateinit var loading : LoadingScene
    val entities : HashMap<Short, Entity> = HashMap()
    val allyColor : Color = Color(0f, 0.341176471f, 0.7725490196f, 1f)
    val enemyColor : Color = Color(0.7725490196f, 0f, 0f, 1f)
    public var disconnected = false
    public var dissconnectScene : Scene? = null
    public var dissconnectScene2 : Scene? = null

    override fun start() {
        loading = LoadingScene()
        Ghost.getInstance().addScene(loading)

        world = SpriteScene()
        Ghost.getInstance().addScene(world)
        world.isVisible = false

        Ghost.onMatchFound = P2Runnable { x, y -> matchFound(x, y) }

        loading.setLoadedCallback(Runnable {
            loading.setText("Connecting to server...")
            Thread(Runnable {

                System.out.println("Connecting..")

                Ghost.client = PlayerClient.connect(IP, this)
                if (!Ghost.client.isConnected) {
                    loading.setText("Failed to connect to server!");
                    return@Runnable;
                }
                connectToGame()

                loading.setText("Waiting for match info..")

            }).start()
        })
    }

    private fun connectToGame() {
        val packet : SessionPacket = SessionPacket()
        packet.writePacket(Ghost.client, Session);
        if (!Ghost.client.ok()) {
            System.out.println("Bad session!");
            throw IOException("Bad session!");
        }

        var tries = 0
        while (true) {
            try {
                Ghost.client.connectUDP(Session)
                if (!Ghost.client.ok(30000L)) {
                    System.out.println("Bad session!");
                    throw IOException("Bad session!");
                }
                break;
            }
            catch (e: TimeoutException) {
                tries++;
                if (tries < 10)
                    System.out.println("Timeout exceeded! Attempting to connect again (attempt " + tries)
                else
                    throw IOException("Could not connect via UDP!");
            }
        }
    }

    private var lastAttempt = 0L
    override fun tick() {
        if (disconnected) {
            if (System.currentTimeMillis() - lastAttempt >= 10000) {
                Thread(Runnable {
                    Ghost.client = PlayerClient.connect(Ghost.client.ip + ":" + Ghost.client.port, this)
                    if (!disconnected) {
                        connectToGame()
                    }
                }).start()
                lastAttempt = System.currentTimeMillis();
            }
        } else if (lastAttempt != 0L) {
            lastAttempt = 0L
            dissconnectScene?.replaceWith(world)
            if (dissconnectScene2 != null)
                Ghost.getInstance().removeScene(dissconnectScene2 as Scene)
        }
    }

    fun matchFound(startX: Float, startY: Float) {
        Gdx.app.postRunnable {
            if (startX != -1f && startY != -1f) {
                player1 = InputEntity(0)
                player1.velocity = Vector2f(0f, 0f)
                player1.setCenter(startX, startY)
                world.addEntity(player1)
            }

            Ghost.isInMatch = true
            Ghost.isReady = false
            Ghost.matchStarted = false

            Ghost.client.acceptUDPPackets()
        }
    }

    public fun spawn(type : Short, id : Short, name : String, x : Float, y : Float, angle : Double, width : Short, height : Short) {
        if (entities.containsKey(id)) {
            //The server claims this ID has already either despawned or does not exist yet
            //As such, I should remove and despawn any sprite that has this ID
            val entity : Entity? = entities.get(id)
            if (entity != null) {
                world.removeEntity(entity)
            }
            entities.remove(id)
        }

        if (type == 0.toShort() || type == 1.toShort()) {
            var player : NetworkPlayer = NetworkPlayer(id, name)
            player.setCenter(x, y)
            player.color = if (type == 0.toShort()) allyColor else enemyColor
            world.addEntity(player)
            entities.put(id, player)

            var username : Text = Text(24, Color.WHITE, Gdx.files.internal("fonts/INFO56_0.ttf"))
            username.y = player.centerY + 32f
            username.x = player.centerX
            username.text = name
            player.attach(username)
            world.addEntity(username)
        } else {
            var entity : Entity? = EntityFactory.createEntity(type, id, x, y, width.toFloat(), height.toFloat())
            if (entity == null) {
                System.err.println("An invalid entity ID was sent by the server!");
                return;
            }

            entity.setOrigin(entity.width / 2f, entity.height / 2f)
            entity.rotation = Math.toDegrees(angle).toFloat()

            world.addEntity(entity)
            entities.put(id, entity)
        }
    }

    fun despawn(id: Short) {
        if (entities.containsKey(id)) {
            val entity : Entity? = entities.get(id)
            if (entity != null) {
                world.removeEntity(entity);
                entities.remove(id)
            }
        }
    }

    fun findEntity(id: Short): Entity? {
        if (id == 0.toShort() || id == Ghost.PLAYER_ENTITY_ID) {
            return player1
        }

        return entities.get(id)
    }

    fun prepareMap(mapName: String) {
        //TODO Prepare the map

        world.isVisible = true
        Ghost.getInstance().removeScene(loading)

        //Once all loaded, ready up
        Ghost.client.setReady(true)
    }

    fun updateStatus(status: Boolean, reason: String) {
        Ghost.matchStarted = status

        if (statusText == null) {
            statusText = Text(28, Color.WHITE, Gdx.files.internal("fonts/INFO56_0.ttf"))

            statusText?.x = 1024 / 2f
            statusText?.y = 130f
            world.addEntity(statusText as Text)
        } else {
            statusText?.text = reason
            statusText?.x = (1024 / 2f)
        }
    }

    fun endMatch() {
        Ghost.client.disconnect()

        System.exit(0)
    }
}
