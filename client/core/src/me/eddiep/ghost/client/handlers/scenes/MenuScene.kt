package me.eddiep.ghost.client.handlers.scenes

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import me.eddiep.ghost.client.core.render.Text
import me.eddiep.ghost.client.core.render.scene.AbstractScene

class MenuScene : AbstractScene() {

    private lateinit var header: Text;
    private lateinit var stage: Stage;
    override fun onInit() {
        header = Text(72, Color.WHITE, Gdx.files.internal("fonts/INFO56_0.ttf"));
        header.x = 512f
        header.y = 520f
        header.text = "PROJECT\nGHOST"
        header.load()

        stage = Stage()
        Gdx.input.inputProcessor = stage

        val skin = Skin(Gdx.files.internal("sprites/ui/uiskin.json"))

        var table = Table()
        table.width = 200f
        table.height = 300f
        table.x = 512f - (table.width / 2f)
        table.y = 300f - (table.height / 2f)
        stage.addActor(table)

        val button = TextButton("PLAY", skin)
        val button2 = TextButton("SETTINGS", skin)
        val button3 = TextButton("QUIT", skin)
        table.add(button).width(130f).height(40f).padBottom(20f)
        table.row()
        table.add(button2).width(130f).height(40f).padBottom(20f)
        table.row()
        table.add(button3).width(130f).height(40f)

     //   table.debug = true
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
