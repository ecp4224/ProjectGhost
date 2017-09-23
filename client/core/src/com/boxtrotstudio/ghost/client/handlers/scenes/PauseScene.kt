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
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.core.render.Text
import com.boxtrotstudio.ghost.client.core.render.scene.AbstractScene
import com.boxtrotstudio.ghost.client.handlers.GameHandler
import com.boxtrotstudio.ghost.client.handlers.MenuHandler

class PauseScene(val gameHandler: GameHandler) : AbstractScene() {
    private lateinit var header: Text;
    private lateinit var stage: Stage;
    override fun onInit() {
        requestOrder(-2)

        header = Text(72, Color.WHITE, Gdx.files.internal("fonts/7thservicebold.ttf"));
        header.x = 640f
        header.y = 520f
        header.text = "Paused"
        header.load()

        stage = Stage(
                Ghost.getInstance().viewport,
                Ghost.getInstance().batch
        )
        attachStage(stage)

        val skin = Skin(Gdx.files.internal("sprites/ui/uiskin.json"))

        Ghost.setStage(stage, skin)

        var table = Table()
        table.width = 200f
        table.height = 300f
        table.x = 640f - (table.width / 2f)
        table.y = 300f - (table.height / 2f)
        stage.addActor(table)

        val button = TextButton("Resume", skin)
        val button4 = TextButton("Settings", skin)
        val button2 = TextButton("Exit to Menu", skin)
        val button3 = TextButton("Exit to Desktop", skin)
        table.add(button).width(130f).height(40f).padBottom(20f)
        table.row()
        table.add(button4).width(130f).height(40f).padBottom(20f)
        table.row()
        table.add(button2).width(130f).height(40f).padBottom(20f)
        table.row()
        table.add(button3).width(130f).height(40f)

        button.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                gameHandler.resume()
            }
        })

        button2.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                //gameHandler.disconnect()
                Ghost.client = null

                Gdx.app.postRunnable {
                    Ghost.getInstance().clearScreen()
                    val menu = MenuHandler()
                    menu.start()
                    Ghost.getInstance().handler = menu
                }
            }
        })

        button3.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                Ghost.exitDialog(skin).show(stage)
            }
        })

        button4.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                replaceWith(OptionScene(this@PauseScene))
                wasInit = false
            }
        })
    }

    override fun render(camera: OrthographicCamera, batch: SpriteBatch) {
        batch.begin()
        header.draw(batch)
        batch.end()

        stage.act()
        stage.draw()
    }

    override fun dispose() {
        stage.dispose()
    }
}
