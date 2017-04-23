package com.boxtrotstudio.ghost.client.handlers.scenes

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.core.game.SpriteEntity
import com.boxtrotstudio.ghost.client.core.render.scene.AbstractScene

public class LoadingScene() : AbstractScene() {
    private lateinit var logo: Sprite
    private var startTime = 0L
    private var stage2 = 0
    private var didCall = false
    private var onFinished = Runnable {  }

    override fun onInit() {
        val logoTexture = Texture("sprites/boxtrotlogo.png")
        logo = Sprite(logoTexture)
        //640, 360
        logo.setCenter(900f, 360f)
        logo.setOriginCenter()

        //rgb(96,62,40)
        Ghost.getInstance().backColor = Color(96f/255f, 62f/255f, 40f/255f, 1f)

        requestOrder(1)
        Ghost.loadGameAssets(Ghost.ASSETS)

        startTime = System.currentTimeMillis()

        if (Ghost.ASSETS.update()) { //If there's nothing to load
            isVisible = false
            onFinished.run()
        }
    }

    override fun render(camera: OrthographicCamera, batch: SpriteBatch) {
        if (Ghost.ASSETS.update() && !didCall) {
            stage2 = 1
            startTime = System.currentTimeMillis()
            didCall = true
        }

        if (stage2 == 1) {
            val pos = SpriteEntity.ease(900f, 640f, 900f, (System.currentTimeMillis() - startTime).toFloat())
            val r = SpriteEntity.ease(96f / 255f, 1f, 900f, (System.currentTimeMillis() - startTime).toFloat())
            val g = SpriteEntity.ease(62f / 255f, 1f, 900f, (System.currentTimeMillis() - startTime).toFloat())
            val b = SpriteEntity.ease(40f / 255f, 1f, 900f, (System.currentTimeMillis() - startTime).toFloat())

            Ghost.getInstance().backColor = Color(r, g, b, 1f)
            logo.setCenter(pos, 360f)

            if (r == 1f) {
                stage2 = 2
                startTime = System.currentTimeMillis()
            }
        }

        if (stage2 == 2 && System.currentTimeMillis() - startTime >= 3000) {
            stage2 = 3
            onFinished.run()
        }

        batch.begin()
        logo.draw(batch)
        batch.end()
    }

    public fun isLoaded() : Boolean {
        return Ghost.ASSETS.update()
    }

    override fun dispose() {
        Ghost.getInstance().backColor = Color.BLACK
    }

    public fun setLoadedCallback(callback: Runnable) {
        this.onFinished = callback
    }
}
