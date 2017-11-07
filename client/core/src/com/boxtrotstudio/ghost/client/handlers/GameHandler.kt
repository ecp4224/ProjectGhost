package com.boxtrotstudio.ghost.client.handlers

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.core.game.CharacterCreator
import com.boxtrotstudio.ghost.client.core.game.Entity
import com.boxtrotstudio.ghost.client.core.game.EntityFactory
import com.boxtrotstudio.ghost.client.core.game.SpriteEntity
import com.boxtrotstudio.ghost.client.core.game.maps.MapCreator
import com.boxtrotstudio.ghost.client.core.game.sprites.InputEntity
import com.boxtrotstudio.ghost.client.core.game.sprites.NamePlate
import com.boxtrotstudio.ghost.client.core.logic.Handler
import com.boxtrotstudio.ghost.client.core.render.Text
import com.boxtrotstudio.ghost.client.core.render.scene.Scene
import com.boxtrotstudio.ghost.client.core.render.text.TextOptions
import com.boxtrotstudio.ghost.client.handlers.scenes.*
import com.boxtrotstudio.ghost.client.network.PlayerClient
import com.boxtrotstudio.ghost.client.network.packets.PingPongPacket
import com.boxtrotstudio.ghost.client.network.packets.SessionPacket
import com.boxtrotstudio.ghost.client.utils.P2Runnable
import com.boxtrotstudio.ghost.client.utils.Vector2f
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeoutException

class GameHandler(val IP : String, val Session : String) : Handler {
    var ambiantColor: Color = Color(1f, 1f, 1f, 1f)
    var ambiantPower : Float = 1f

    var player1 : InputEntity? = null

    public lateinit var world : SpriteScene
    lateinit var loading : LoadingScene
    lateinit var blurred : BlurredScene
    lateinit var overlay : TextOverlayScene
    var bestOfOverlay : BestOfOverlay? = null

    var didLoad = false
    val entities : HashMap<Short, Entity> = HashMap()
    val allyColor : Color = Color(0f, 0.341176471f, 0.7725490196f, 1f)
    val enemyColor : Color = Color(0.7725490196f, 0f, 0f, 1f)
    public var disconnected = false
    public var dissconnectScene : Scene? = null
    public var dissconnectScene2 : Scene? = null

    public var didWin1 = -1
    public var didWin2 = -1
    public var didWin3 = -1

    var texts = HashMap<Long, Text>()

    private fun loadThenStart() {
        didLoad = true
        loading = LoadingScene()
        Ghost.getInstance().addScene(loading)

        world = SpriteScene()
        world.isVisible = false

        Ghost.onMatchFound = P2Runnable { x, y -> matchFound(x, y) }

        blurred = BlurredScene(world, 17f)
        blurred.requestOrder(-1)

        Ghost.getInstance().addScene(blurred)
        Ghost.getInstance().addScene(world)

        overlay = TextOverlayScene("Loading", "", false)
        overlay.isVisible = false
        Ghost.getInstance().addScene(overlay)

        Ghost.PHYSICS.clear()

        loading.setLoadedCallback(Runnable {
            //loading.setText("Connecting to server...")
            Thread(Runnable {

                System.out.println("Connecting..")

                if (Ghost.client == null) {
                    Ghost.client = PlayerClient.connect(IP, this)
                    if (!Ghost.client.isConnected) {
                        //loading.setText("Failed to connect to server!");
                        return@Runnable;
                    }
                } else {
                    Ghost.client.game = this;
                }
                connectToGame()

                //loading.setText("Waiting for match info..")

            }).start()
        })
    }

    override fun start() {
        Ghost.getInstance().clearBodies()

        if (Ghost.rayHandler != null)
            Ghost.rayHandler.removeAll()

        Ghost.loadGameAssets(Ghost.ASSETS)

        if (Ghost.ASSETS.progress < 1) {
            loadThenStart()
        } else {
            world = SpriteScene()

            Ghost.onMatchFound = P2Runnable { x, y -> matchFound(x, y) }

            blurred = BlurredScene(world, 17f)
            blurred.requestOrder(-1)

            Ghost.getInstance().addScene(blurred)
            Ghost.getInstance().addScene(world)

            overlay = TextOverlayScene("Loading", "", false)
            overlay.isVisible = false
            Ghost.getInstance().addScene(overlay)

            Ghost.PHYSICS.clear()

            Thread(Runnable {
                if (Ghost.client == null || !Ghost.client.isConnected) {
                    System.out.println("Connecting..")
                    Ghost.client = PlayerClient.connect(IP, this)
                    if (!Ghost.client.isConnected) {
                        return@Runnable
                    }
                } else {
                    Ghost.client.game = this;
                }
                connectToGame()
            }).start()
        }
    }

    private fun connectToGame() {
        if (!Ghost.client.isValidated) {
            val packet: SessionPacket = SessionPacket()
            packet.writePacket(Ghost.client, Session);
            if (!Ghost.client.ok()) {
                System.out.println("Bad session!");
                throw IOException("Bad session!");
            }
            Ghost.client.isValidated = true
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
    private var lastPingTime: Long = System.currentTimeMillis()

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

        if (System.currentTimeMillis() - lastPingTime > 5000 && Ghost.client.isUDPConnected) {
            PingPongPacket().writePacket(Ghost.client)
            lastPingTime = System.currentTimeMillis()
        }
    }

    fun matchFound(startX: Float, startY: Float) {
        Gdx.app.postRunnable {
            if (startX != -1f && startY != -1f) {
                //player1 = InputEntity(0)
                player1 = CharacterCreator.createPlayer(Ghost.selfCharacter, "DEFAULT", 0)
                player1?.velocity = Vector2f(0f, 0f)
                val namePlate = NamePlate(Ghost.username)
                namePlate.setCenter(73f, 12f)
                player1?.setCenter(startX, startY)
                world.addEntity(player1 as SpriteEntity)
                world.addEntity(namePlate)
            }

            Ghost.isInMatch = true
            Ghost.isReady = false
            Ghost.matchStarted = false

            Ghost.client.acceptUDPPackets()
        }
    }

    public fun spawn(type: Short, id: Short, name: String, x: Float, y: Float, angle: Double, width: Short, height: Short, hasLighting: Boolean) {
        if (name == Ghost.username)
            return //Ignore ourselves

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
            //TODO Save which skin to use
            //var play = NetworkPlayer(id, name)
            var player = CharacterCreator.createNetworkPlayer(if (type == 0.toShort()) Ghost.allies[name] else Ghost.enemies[name], "DEFAULT", id)
            player.setCenter(x, y)

            if (player.isDot)
                player.color = if (type == 0.toShort()) allyColor else enemyColor

            val namePlate = NamePlate(name)
            namePlate.setCenter(1211f, 12f)

            world.addEntity(player)
            world.addEntity(namePlate)
            entities.put(id, player)
        } else {
            val entity: Entity? =
                    if (type.toInt() != -3)
                        EntityFactory.createEntity(type, id, x, y, width.toFloat(), height.toFloat(), angle.toFloat(), name)
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

            if (type.toInt() != -3 && type.toInt() != 93 && entity is SpriteEntity) {
                entity.setOrigin(entity.width / 2f, entity.height / 2f)
                entity.z = -1
            } else {
                entity.x = x
                entity.y = y
                entity.setSize(width.toFloat(), height.toFloat())
                entity.rotation = angle.toFloat()

                entity.z = -1
                entity.setHasLighting(hasLighting)
            }

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
            if (player1 == null)
                return null
            return player1
        }

        return entities.get(id)
    }

    fun prepareMap(mapName: String) {
        didWin1 = -1
        didWin2 = -1
        didWin3 = -1

        System.out.println("Loading map " + mapName)
        for (m in MapCreator.MAPS) {
            if (m.name().equals(mapName))
                m.construct(world)
        }

        blurred.isVisible = true
        world.isVisible = true
        overlay.isVisible = true

        if (didLoad) {
            Ghost.getInstance().removeScene(loading)
        }

        //Once all loaded, ready up
        do {
            Ghost.client.setReady(true)

            var ok = false
            try {
                ok = Ghost.client.ok(5000L)
            } catch (e: Throwable) {
                e.printStackTrace()
            }

        } while (!ok)

        System.err.println("Got OK")
    }

    fun updateStatus(status: Boolean, reason: String) {
        Gdx.app.postRunnable {
            if (status && !Ghost.matchStarted) {
                blurred.replaceWith(world)

                if (bestOfOverlay != null) {
                    Ghost.getInstance().removeScene(bestOfOverlay as BestOfOverlay)
                    bestOfOverlay = null
                }

            } else if (!status && Ghost.matchStarted) {
                world.replaceWith(blurred)
                if (reason == "Round Won!" || reason == "Round Lost.." || reason == "Round Over") {
                    blurred.fadeIn(Runnable {
                        bestOfOverlay = BestOfOverlay(didWin1, didWin2, didWin3)
                        Ghost.getInstance().addScene(bestOfOverlay as BestOfOverlay)
                    })
                } else {
                    blurred.fadeIn(null)
                }
            }

            Ghost.matchStarted = status
            overlay.setHeaderText(reason)
        }

        if (reason.equals("Game canceled! Not enough players connected")) {
            Gdx.app.postRunnable {
                val statScreen = StatsScene(0, 0, false, 0, false, this)
                statScreen.requestOrder(-5)
                Ghost.getInstance().addScene(statScreen)
            }
        }
    }

    fun endMatch() {
        if (bestOfOverlay != null) {
            Ghost.getInstance().removeScene(bestOfOverlay as BestOfOverlay)
            bestOfOverlay = null
        }
    }


    public var isPaused = false
    lateinit var pauseMenu: PauseScene

    fun togglePause() {
        if (isPaused) {
            resume()
        } else {
            pause()
        }
    }

    fun pause() {
        if (isPaused)
            return

        isPaused = true
        Gdx.app.postRunnable {
            pauseMenu = PauseScene(this)
            Ghost.getInstance().addScene(pauseMenu)
            world.replaceWith(blurred)
        }
    }

    fun resume() {
        if (!isPaused)
            return

        isPaused = false
        Gdx.app.postRunnable {
            blurred.fadeOut(Runnable {
                blurred.replaceWith(world)
            })
            Ghost.getInstance().removeScene(pauseMenu)
        }
    }

    fun disconnect() {
        Ghost.client.disconnect()
    }

    fun displayText(text: String?, size: Int, color: Color, x: Float, y: Float, options: Int, id: Long) {
        val textEntity = Text(size, color, Gdx.files.internal("fonts/TitilliumWeb-Regular.ttf"))
        textEntity.x = x
        textEntity.y = y
        textEntity.text = text

        TextOptions.values()
                .filter { (options and it.flag) == it.flag }
                .forEach { it.apply(textEntity) }

        texts.put(id, textEntity)

        world.addEntity(textEntity)
    }

    fun removeText(id: Long) {
        if (texts.containsKey(id)) {
            val text = texts[id]

            if (text != null) {
                world.removeEntity(text)
            }
            texts.remove(id)
        }
    }
}
