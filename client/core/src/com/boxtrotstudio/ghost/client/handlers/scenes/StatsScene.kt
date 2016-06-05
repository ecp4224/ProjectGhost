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
import com.boxtrotstudio.ghost.client.core.render.Drawable
import com.boxtrotstudio.ghost.client.core.render.Text
import com.boxtrotstudio.ghost.client.core.render.scene.AbstractScene
import com.boxtrotstudio.ghost.client.handlers.MenuHandler
import java.util.*

class StatsScene(val shots: Int, val hits: Int, val hatTrick: Boolean, val itemUsage: Int, val showStats: Boolean) : AbstractScene() {
    private var toDraw: ArrayList<Drawable> = ArrayList()
    private lateinit var stage: Stage;
    override fun onInit() {
        stage = Stage(
                ScalingViewport(Scaling.stretch, 1280f, 720f, OrthographicCamera()),
                Ghost.getInstance().batch
        )
        Gdx.input.inputProcessor = stage

        val skin = Skin(Gdx.files.internal("sprites/ui/uiskin.json"))

        var buttonTable = Table()
        buttonTable.width = 300f
        buttonTable.height = 40f
        buttonTable.x = 640f - (buttonTable.width / 2f)
        buttonTable.y = 40f - (buttonTable.height / 2f)
        stage.addActor(buttonTable)

        val mainMenu = TextButton("Main Menu", skin)
        buttonTable.add(mainMenu).width(130f).height(40f)

        mainMenu.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                Ghost.client.disconnect()
                Ghost.client = null

                Gdx.app.postRunnable {
                    Ghost.getInstance().clearScreen()
                    val menu = MenuHandler()
                    menu.start()
                    Ghost.getInstance().handler = menu
                }
            }
        })

        if (showStats) {
            //1st icon = 365,450
            //2nd icon = 640,450
            //3rd icon = 895,450

            //val shotsIcon = Text(36, Color(0.674509804f, 0f, 0f, 1f), Gdx.files.internal("fonts/fontawesome.ttf"));
            val shotsIcon = Text(72, Color.WHITE, Gdx.files.internal("fonts/fontawesome.ttf"), "\uf05b");
            shotsIcon.x = 365f
            shotsIcon.y = 720 - 450f
            shotsIcon.text = "\uf05b"
            shotsIcon.load()

            val hitsIcon = Text(72, Color(45 / 255f, 140f / 255f, 7f / 255f, 1f), Gdx.files.internal("fonts/fontawesome.ttf"), "\uf140");
            hitsIcon.x = 640f
            hitsIcon.y = 720 - 450f
            hitsIcon.text = "\uf140"
            hitsIcon.load()

            //rgb(23,104,178)
            val itemUsageIcon = Text(72, Color(23f / 255f, 104f / 255f, 178f / 255f, 1f), Gdx.files.internal("fonts/fontawesome.ttf"), "\uf06b");
            itemUsageIcon.x = 895f
            itemUsageIcon.y = 720 - 450f
            itemUsageIcon.text = "\uf06b"
            itemUsageIcon.load()

            if (this.hatTrick) {
                val hatTrick = Text(72, Color(252f / 255f, 231f / 255f, 80f / 255f, 1f), Gdx.files.internal("fonts/fontawesome.ttf"), "\uf005");
                hatTrick.x = 640f
                hatTrick.y = 450f
                hatTrick.text = "\uf005"
                hatTrick.load()
                toDraw.add(hatTrick)
            }


            toDraw.add(shotsIcon)
            toDraw.add(hitsIcon)
            toDraw.add(itemUsageIcon)

            val accuracy = if (shots != 0) ((hits.toDouble() / shots.toDouble()) * 100.0).toInt() else 0;

            val shotsText = Text(36, Color.WHITE, Gdx.files.internal("fonts/INFO56_0.ttf"));
            shotsText.x = 365f
            shotsText.y = 720 - 520f
            shotsText.text = "$shots Shot" + if (shots > 1) "s" else ""
            shotsText.load()

            val hitsText = Text(36, Color.WHITE, Gdx.files.internal("fonts/INFO56_0.ttf"));
            hitsText.x = 640f
            hitsText.y = 720 - 520f
            hitsText.text = "$accuracy% Accuracy"
            hitsText.load()

            val itemUsageText = Text(36, Color.WHITE, Gdx.files.internal("fonts/INFO56_0.ttf"));
            itemUsageText.x = 895f
            itemUsageText.y = 720 - 520f
            itemUsageText.text = "$itemUsage Item" + if (itemUsage > 1) "s" else "" + " Used"
            itemUsageText.load()

            toDraw.add(shotsText)
            toDraw.add(hitsText)
            toDraw.add(itemUsageText)
        }
    }

    override fun render(camera: OrthographicCamera, batch: SpriteBatch) {
        if (showStats) {
            batch.begin()
            for (drawable in toDraw) {
                drawable.draw(batch)
            }
            batch.end()
        }

        stage.draw()
        stage.act()
    }

    override fun dispose() {
        toDraw.clear()
        stage.dispose()
    }

}
