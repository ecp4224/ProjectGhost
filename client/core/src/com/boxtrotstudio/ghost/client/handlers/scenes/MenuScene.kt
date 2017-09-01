package com.boxtrotstudio.ghost.client.handlers.scenes

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.core.game.SpriteEntity
import com.boxtrotstudio.ghost.client.core.render.Text
import com.boxtrotstudio.ghost.client.core.render.scene.AbstractScene
import com.boxtrotstudio.ghost.client.handlers.GameHandler
import com.boxtrotstudio.ghost.client.network.packets.JoinQueuePacket
import com.boxtrotstudio.ghost.client.utils.GlobalOptions
import com.boxtrotstudio.ghost.client.utils.P2Runnable
import java.util.*

class MenuScene : AbstractScene() {

    private var currentGrid = 0
    private val gridCount = 4

    private var gridStartTime = 0L
    private lateinit var gridfrom: Sprite
    private lateinit var gridto: Sprite
    private var grids = ArrayList<Sprite>()
    private lateinit var bricks: Sprite
    private lateinit var overlay: Sprite
    private lateinit var logo: Sprite
    override fun onInit() {
        for (i in 1..gridCount) {
            grids.add(Sprite(Ghost.ASSETS.get("sprites/ui/start/grid_$i.png", Texture::class.java)))
        }
        gridto = grids[0]
        nextGrid()


        bricks = Sprite(Ghost.ASSETS.get("sprites/ui/start/brick.png", Texture::class.java))
        overlay = Sprite(Ghost.ASSETS.get("sprites/ui/start/overlay.png", Texture::class.java))

        logo = Sprite(Ghost.ASSETS.get("sprites/ui/start/logo.png", Texture::class.java))
        logo.setCenter(1280f / 2f, 720f / 1.25f)
    }

    fun nextGrid() {
        currentGrid++

        if (currentGrid >= gridCount) {
            currentGrid = 0
        }

        gridfrom = gridto
        gridto = grids[currentGrid]
        gridStartTime = System.currentTimeMillis()
    }

    override fun render(camera: OrthographicCamera, batch: SpriteBatch) {
        val alphafrom = SpriteEntity.ease(1f, 0f, 3800f, (System.currentTimeMillis() - gridStartTime).toFloat())
        val alphato = SpriteEntity.ease(0f, 1f, 3800f, (System.currentTimeMillis() - gridStartTime).toFloat())

        gridfrom.setAlpha(alphafrom)
        gridto.setAlpha(alphato)

        if (alphafrom == 0f) {
            nextGrid()
        }

        batch.begin()
        gridfrom.draw(batch)
        gridto.draw(batch)
        bricks.draw(batch)
        overlay.draw(batch)
        logo.draw(batch)
        batch.end()
    }

    override fun dispose() {

    }

    fun startTutorial() {
        val packet2 = JoinQueuePacket()
        packet2.writePacket(Ghost.matchmakingClient, 6.toByte())

        /*checkerToken = Timer.newTimer({
            playersInQueue = Integer.parseInt(WebUtils.readContentsToString(URL("http://" + Ghost.getIp() + ":8080/queue/8")))
        }, 3000L)*/

        Ghost.onMatchFound = P2Runnable { x, y ->
            Gdx.app.postRunnable {
                Ghost.matchmakingClient.disconnect()
                Ghost.matchmakingClient = null

                Ghost.getInstance().clearScreen()
                val game = GameHandler(Ghost.getIp(), Ghost.Session)
                game.start()
                Ghost.getInstance().handler = game
            }
        }
    }
}
