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
    private val gridCount = 4

    private var gridStartTime = 0L
    private lateinit var gridfrom: Sprite
    private lateinit var gridto: Sprite
    private var grids = ArrayList<Sprite>()

    private lateinit var bricks: Sprite
    private lateinit var overlay: Sprite
    override fun onInit() {
        for (i in 1..gridCount) {
            grids.add(Sprite(Ghost.ASSETS.get("sprites/ui/start/grid_$i.png", Texture::class.java)))
            grids[i - 1].setAlpha(0f)
        }
        grids[0].setAlpha(1f)
        gridto = grids[0]
        nextGrid()


        bricks = Sprite(Ghost.ASSETS.get("sprites/ui/start/brick.png", Texture::class.java))
        overlay = Sprite(Ghost.ASSETS.get("sprites/ui/start/overlay.png", Texture::class.java))
    }

    fun nextGrid() {
        currentGrid++

        if (currentGrid >= gridCount) {
            currentGrid = 0
        }

        gridfrom = gridto
        gridto = grids[currentGrid]
        gridStartTime = System.currentTimeMillis()
    }

    override fun render(camera: OrthographicCamera, batch: SpriteBatch) {
        val alphafrom = SpriteEntity.ease(1f, 0f, 3800f, (System.currentTimeMillis() - gridStartTime).toFloat())
        val alphato = SpriteEntity.ease(0f, 1f, 3800f, (System.currentTimeMillis() - gridStartTime).toFloat())

        gridfrom.setAlpha(alphafrom)
        gridto.setAlpha(alphato)

        if (alphafrom == 0f) {
            nextGrid()
        }

        batch.begin()
        gridfrom.draw(batch)
        gridto.draw(batch)
        bricks.draw(batch)
        overlay.draw(batch)
        batch.end()
    }

    override fun dispose() {
    }

}