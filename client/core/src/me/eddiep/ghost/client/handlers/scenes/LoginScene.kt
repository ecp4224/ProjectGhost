package me.eddiep.ghost.client.handlers.scenes

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.utils.Align
import me.eddiep.ghost.client.core.render.Text
import me.eddiep.ghost.client.core.render.scene.AbstractScene

class LoginScene : AbstractScene() {
    private lateinit var header: Text;
    private lateinit var stage: Stage;
    override fun onInit() {
        header = Text(72, Color.WHITE, Gdx.files.internal("fonts/INFO56_0.ttf"));
        header.x = 512f
        header.y = 520f
        header.text = "LOGIN"
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

        val username = TextField("", skin)
        username.messageText = "USERNAME"
        val password = TextField("", skin)
        password.setPasswordCharacter('*')
        password.isPasswordMode = true
        password.messageText = "PASSWORD"

        val loginButton = TextButton("LOGIN", skin)

        username.setAlignment(Align.center)
        password.setAlignment(Align.center)

        table.add(username).width(130f).height(30f).padBottom(30f)
        table.row()
        table.add(password).width(130f).height(30f).padBottom(30f)
        table.row()
        table.add(loginButton).width(100f).height(35f)

        //table.debug = true
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
