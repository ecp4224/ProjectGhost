package com.boxtrotstudio.ghost.client.handlers.scenes

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.core.game.DynamicAnimation
import com.boxtrotstudio.ghost.client.core.game.SpriteEntity
import com.boxtrotstudio.ghost.client.core.render.scene.AbstractScene
import com.boxtrotstudio.ghost.client.handlers.GameHandler
import com.boxtrotstudio.ghost.client.network.packets.JoinQueuePacket
import com.boxtrotstudio.ghost.client.utils.P2Runnable
import com.boxtrotstudio.ghost.client.utils.PRunnable
import java.util.*

class MenuScene : AbstractScene() {
    private val BUTTON_DURATION = 200

    private var spinSpeed = 1f
    private var currentAnimation : DynamicAnimation? = null

    private lateinit var logo: Sprite
    private lateinit var stage: Stage

    private var topRow = ArrayList<ImageButton>()
    private var bottomRow = ArrayList<ImageButton>()

    private lateinit var selected: Sprite
    override fun onInit() {
        logo = Sprite(Ghost.ASSETS.get("sprites/ui/start/logo.png", Texture::class.java))
        logo.setCenter(1280f / 2f, 720f / 1.25f)

        selected = Sprite(Ghost.ASSETS.get("sprites/ui/start/selected.png", Texture::class.java))
        hideSelector()

        stage = Stage(
                Ghost.getInstance().viewport,
                Ghost.getInstance().batch
        )
        attachStage(stage)

        var topRowTable = Table()
        topRowTable.width = 1280f
        topRowTable.height = 200f
        topRowTable.x = 640f - (topRowTable.width / 2f)
        topRowTable.y = 50f

        //topRowTable.debug = true

        val ranked = ImageButton(grabDrawable("sprites/ui/start/ranked_match.png"))
        val normals = ImageButton(grabDrawable("sprites/ui/start/normal_match.png"))
        val tutorial = ImageButton(grabDrawable("sprites/ui/start/tutorial.png"))

        topRowTable.add(ranked).padRight(20f)
        topRowTable.add(normals).padRight(20f)
        topRowTable.add(tutorial)

        ranked.isTransform = true
        ranked.setOrigin(Align.center)
        ranked.setScale(0.75f)

        normals.isTransform = true
        normals.setOrigin(Align.center)
        normals.setScale(0.75f)

        tutorial.isTransform = true
        tutorial.setOrigin(Align.center)
        tutorial.setScale(0.75f)

        stage.addActor(topRowTable)


        var bottomRowTable = Table()
        bottomRowTable.width = 1280f
        bottomRowTable.height = 200f
        bottomRowTable.x = 640f - (topRowTable.width / 2f)
        bottomRowTable.y = -50f

        //topRowTable.debug = true

        val options = ImageButton(grabDrawable("sprites/ui/start/options.png"), grabDrawable("sprites/ui/start/options_selected.png"))
        val exit = ImageButton(grabDrawable("sprites/ui/start/exit.png"), grabDrawable("sprites/ui/start/exit_selected.png"))

        bottomRowTable.add(options).padRight(120f).padLeft(-50f)
        bottomRowTable.add(exit)

        stage.addActor(bottomRowTable)


        ranked.addListener(object : ClickListener() {
            override fun enter(event: InputEvent?, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                showSelectorOn(ranked)
            }

            override fun exit(event: InputEvent?, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                hideSelector()
                ranked.setScale(0.75f)
            }

            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
            }
        })

        normals.addListener(object : ClickListener() {
            override fun enter(event: InputEvent?, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                showSelectorOn(normals)
            }

            override fun exit(event: InputEvent?, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                hideSelector()
                normals.setScale(0.75f)
            }

            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
            }
        })

        tutorial.addListener(object : ClickListener() {
            override fun enter(event: InputEvent?, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                showSelectorOn(tutorial)
            }

            override fun exit(event: InputEvent?, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                hideSelector()
                tutorial.setScale(0.75f)
            }

            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
            }
        })

        options.addListener(object : ClickListener() {
            override fun enter(event: InputEvent?, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                showSelectorOn(options, true)
            }

            override fun exit(event: InputEvent?, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                hideSelector()
            }

            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
            }
        })

        exit.addListener(object : ClickListener() {
            override fun enter(event: InputEvent?, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                showSelectorOn(exit, true)
            }

            override fun exit(event: InputEvent?, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                hideSelector()
            }

            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
            }
        })
    }

    private fun showSelectorOn(actor: Actor, small: Boolean = false) {
        if (!small) {
            val startX = actor.scaleX
            val startY = actor.scaleY
            currentAnimation = DynamicAnimation(PRunnable { time ->
                val nx = SpriteEntity.ease(startX, 1f, BUTTON_DURATION.toFloat(), time.toFloat())
                val ny = SpriteEntity.ease(startY, 1f, BUTTON_DURATION.toFloat(), time.toFloat())

                actor.scaleX = nx
                actor.scaleY = ny
            }, BUTTON_DURATION.toLong()).start()
        }

        if (small)
            selected.setScale(0.5f)
        else
            selected.setScale(1f)

        val endX = actor.x + (actor.width / 2f)
        val endY = if (!small) actor.y + (actor.height / 1.25f) else actor.y

        selected.setCenter(endX, endY)
    }

    private fun hideSelector() {
        currentAnimation?.end()
        selected.setCenter(-400f, -400f)
    }

    fun grabDrawable(path: String) : Drawable {
        return TextureRegionDrawable(TextureRegion(Ghost.ASSETS.get(path, Texture::class.java)))
    }


    override fun render(camera: OrthographicCamera, batch: SpriteBatch) {
        batch.begin()
        logo.draw(batch)
        selected.draw(batch)
        batch.end()

        stage.act()
        stage.draw()
    }

    override fun dispose() {
        stage.dispose()
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
