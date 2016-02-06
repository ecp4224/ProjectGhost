package me.eddiep.ghost.client.handlers.scenes

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import me.eddiep.ghost.client.core.render.Text
import me.eddiep.ghost.client.core.render.scene.AbstractScene

class DisconnectedScene : AbstractScene() {
    private lateinit var disconnected : Text
    private lateinit var reconnecting : Text
    var dots = 0
    override fun init() {
        disconnected = Text(36, Color.WHITE, Gdx.files.internal("fonts/INFO56_0.ttf"));
        disconnected.x = 512f
        disconnected.y = 360f
        disconnected.text = "DISCONNECTED"
        disconnected.load()

        reconnecting = Text(28, Color.WHITE, Gdx.files.internal("fonts/INFO56_0.ttf"));
        reconnecting.x = 512f
        reconnecting.y = 300f
        reconnecting.text = "Attempting to reconnect"
        reconnecting.load()

        requestOrder(-2)
    }

    private var lastDot = 0L
    override fun render(camera: OrthographicCamera, batch: SpriteBatch) {

        if (System.currentTimeMillis() - lastDot > 800) {
            dots++;
            if (dots == 4)
                dots = 0;


            reconnecting.text = "Attempting to reconnect"
            for (i in 0..dots) {
                reconnecting.text += "."
            }

            reconnecting.x = 512f

            lastDot = System.currentTimeMillis()
        }

        batch.begin()

        disconnected.draw(batch)
        reconnecting.draw(batch)

        batch.end()
    }

    override fun dispose() {

    }

}
