package com.boxtrotstudio.ghost.client.handlers.scenes

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.core.game.SpriteEntity
import com.boxtrotstudio.ghost.client.core.render.scene.AbstractScene
import java.util.*

class GridScene : AbstractScene() {
    private var currentGrid = 0
    private val gridCount = 5

    private var gridStartTime = 0L
    private lateinit var gridFrom: Sprite
    private lateinit var gridTo: Sprite
    private var grids = ArrayList<Sprite>()

    private var isFading = false
    private var fadeStart = 0f
    private var fadeFrom = 0f
    private var fadeTo = 1f
    private var duration = 0f

    private lateinit var overlay: Sprite
    override fun onInit() {
        for (i in 1..gridCount) {
            grids.add(Sprite(Ghost.ASSETS.get("sprites/ui/start/grid_$i.png", Texture::class.java)))
            grids[i - 1].setAlpha(0f)
        }
        grids[0].setAlpha(1f)
        gridTo = grids[0]
        nextGrid()

        overlay = Sprite(Ghost.ASSETS.get("sprites/ui/start/overlay.png", Texture::class.java))
    }

    fun nextGrid() {
        currentGrid++

        if (currentGrid >= gridCount) {
            currentGrid = 0
        }

        gridFrom = gridTo
        gridTo = grids[currentGrid]
        gridStartTime = System.currentTimeMillis()
    }

    override fun render(camera: OrthographicCamera, batch: SpriteBatch) {
        val alphaFrom = SpriteEntity.ease(1f, 0f, 3800f, (System.currentTimeMillis() - gridStartTime).toFloat())
        val alphaTo = SpriteEntity.ease(0f, 1f, 3800f, (System.currentTimeMillis() - gridStartTime).toFloat())

        gridFrom.setAlpha(alphaFrom)
        gridTo.setAlpha(alphaTo)

        if (alphaFrom == 0f) {
            nextGrid()
        }

        batch.begin()
        gridFrom.draw(batch)
        gridTo.draw(batch)
        overlay.draw(batch)
        batch.end()
    }

    override fun dispose() {
    }
}