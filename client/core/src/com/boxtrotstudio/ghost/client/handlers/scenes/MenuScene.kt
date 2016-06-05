package com.boxtrotstudio.ghost.client.handlers.scenes

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ScalingViewport
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.core.render.Text
import com.boxtrotstudio.ghost.client.core.render.scene.AbstractScene
import com.boxtrotstudio.ghost.client.handlers.GameHandler
import com.boxtrotstudio.ghost.client.network.packets.JoinQueuePacket
import com.boxtrotstudio.ghost.client.utils.GlobalOptions
import com.boxtrotstudio.ghost.client.utils.P2Runnable

class MenuScene : AbstractScene() {

    private lateinit var header: Text;
    private lateinit var stage: Stage;
    override fun onInit() {
        val widthMult = (Gdx.graphics.width / 1280f)
        val heightMult = (Gdx.graphics.height / 720f)

        header = Text(72, Color.WHITE, Gdx.files.internal("fonts/TitilliumWeb-SemiBold.ttf"));
        header.x = 640f * widthMult
        header.y = 520f * heightMult
        header.text = "Project\nGhost"
        header.load()

        stage = Stage(
                ScalingViewport(Scaling.stretch, 1280f, 720f, OrthographicCamera()),
                Ghost.getInstance().batch
        )
        Gdx.input.inputProcessor = stage

        val skin = Skin(Gdx.files.internal("sprites/ui/uiskin.json"))

        Ghost.setStage(stage, skin)

        var table = Table()
        table.width = 200f
        table.height = 300f
        table.x = 640f - (table.width / 2f)
        table.y = 300f - (table.height / 2f)
        stage.addActor(table)

        val button = TextButton("Play", skin)
        val button4 = TextButton("Tutorial", skin)
        val button2 = TextButton("Settings", skin)
        val button3 = TextButton("Quit", skin)
        table.add(button).width(130f).height(40f).padBottom(20f)
        table.row()
        table.add(button4).width(130f).height(40f).padBottom(20f)
        table.row()
        table.add(button2).width(130f).height(40f).padBottom(20f)
        table.row()
        table.add(button3).width(130f).height(40f)

        button.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                replaceWith(GameSetupScene())
            }
        })

        button2.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                replaceWith(OptionScene())
            }
        })

        button3.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                Ghost.exitDialog(skin).show(stage)
            }
        })

        button4.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                startTutorial()
            }
        })
    }

    var didAsk = false
    override fun render(camera: OrthographicCamera, batch: SpriteBatch) {
        batch.begin()
        header.draw(batch)
        batch.end()

        stage.act()
        stage.draw()

        if (!didAsk && GlobalOptions.getOptions().isFirstRun) {
            didAsk = true
            Ghost.createQuestionDialog("Tutorial", "Would you like to start the tutorial?", {
                startTutorial()
            })
        }
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
                Ghost.getInstance().handler = GameHandler(Ghost.getIp(), Ghost.Session)
                Ghost.getInstance().clearScreen()
            }
        }
    }
}
