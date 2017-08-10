package com.boxtrotstudio.ghost.client.handlers.scenes

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.boxtrotstudio.ghost.client.core.render.Drawable
import com.boxtrotstudio.ghost.client.core.render.Text
import com.boxtrotstudio.ghost.client.core.render.scene.AbstractScene
import java.util.*

class BestOfOverlay(val didWin1: Int, val didWin2: Int, val didWin3: Int) : AbstractScene() {
    private var toDraw: ArrayList<Drawable> = ArrayList()
    val star = "\uf005"
    val cross = "\uf00d"
    val empty = "\uf1db"

    val starColor = Color(252f / 255f, 231f / 255f, 80f / 255f, 1f)
    val crossColor = Color(198 / 255f, 40 / 255f, 40 / 255f, 1f)
    val emptyColor = Color.GRAY
    override fun onInit() {
        addToken(didWin1)
        addToken(didWin2)
        addToken(didWin3)

        requestOrder(-3)
    }

    override fun render(camera: OrthographicCamera, batch: SpriteBatch) {
        batch.begin()
        for (drawable in toDraw) {
            drawable.draw(batch)
        }
        batch.end()
    }

    override fun dispose() {
        toDraw.clear()
    }

    var currentX = 426f
    private fun addToken(didWin: Int) {
        val color = if (didWin == 1) starColor else if (didWin == 0) crossColor else emptyColor
        val text = if (didWin == 1) star else if (didWin == 0) cross else empty

        val sprite = Text(42, color, Gdx.files.internal("fonts/fontawesome.ttf"), text)
        sprite.x = currentX
        sprite.y = 250f
        sprite.text = text
        sprite.load()

        toDraw.add(sprite)

        currentX += 200f
    }
}