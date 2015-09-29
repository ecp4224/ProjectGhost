package me.eddiep.ghost.client.handlers

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import me.eddiep.ghost.client.Ghost
import me.eddiep.ghost.client.Handler
import me.eddiep.ghost.client.core.Text

class GameHandler(val IP : String) : Handler {
    lateinit var text : Text

    override fun start() {
        text = Text(24, Gdx.files.getFileHandle("fonts/INFO56_0.ttf"))

        text.x = 512f
        text.y = 360f
        text.text = "Connecting to server..."
        Ghost.getInstance().addEntity(text)
    }

    override fun tick() {

    }
}
