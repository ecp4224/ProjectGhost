package com.boxtrotstudio.ghost.client.handlers.scenes

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.TimeUtils
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.core.game.SpriteEntity
import com.boxtrotstudio.ghost.client.core.render.scene.AbstractScene
import com.boxtrotstudio.ghost.client.core.render.scene.Scene

class BlurredScene(val original: Scene, val maxRadius: Float) : AbstractScene() {
    private lateinit var targetA: FrameBuffer
    private lateinit var targetB: FrameBuffer
    private lateinit var regionA: TextureRegion
    private lateinit var regionB: TextureRegion
    private lateinit var shader: ShaderProgram
    private var radius = maxRadius

    private var fading = false
    private var fadeStart = 0f
    private var fadeEnd = 0f
    private var fadeStartTime = 0L
    private var fadeCallback: Runnable? = null

    override fun init() {
        targetA = FrameBuffer(Pixmap.Format.RGBA8888, original.width, original.height, false)
        regionA = TextureRegion(targetA.colorBufferTexture, targetA.width, targetA.height)
        regionA.flip(false, true) //Flip the y-axis

        targetB = FrameBuffer(Pixmap.Format.RGBA8888, original.width, original.height, false)
        regionB = TextureRegion(targetB.colorBufferTexture, targetB.width, targetB.height)
        regionB.flip(false, true) //Flip the y-axis

        regionA.texture?.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        regionA.texture?.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)

        original.init()

        shader = ShaderProgram(Gdx.files.local("shaders/blur.vert"), Gdx.files.local("shaders/blur.frag"))

        if (shader.log.length !=0)
            System.out.println(shader.log);

        shader.begin()
        shader.setUniformf("dir", 0f, 0f)
        shader.setUniformf("resolution", original.width.toFloat())
        shader.setUniformf("radius", radius)
        shader.end()
    }

    override fun render(camera: OrthographicCamera, batch: SpriteBatch) {
        targetA.begin()

        Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        original.render(camera, batch)

        targetA.end()

        batch.shader = shader

        targetB.begin()

        batch.begin()

        //Calculate fade first, if on
        if (fading) {
            radius = SpriteEntity.ease(fadeStart, fadeEnd,
                    700f, (TimeUtils.millis() - fadeStartTime).toFloat())

            if (radius == fadeEnd) {
                fading = false

                fadeCallback?.run()
                fadeCallback = null
            }

            shader.setUniformf("radius", radius)
        }

        shader.setUniformf("dir", 1f, 0f)
        shader.setUniformf("resolution", original.width.toFloat() * 8f)

        batch.draw(regionA, 0f, 0f)

        batch.flush()
        //batch.end()

        targetB.end()

        shader.setUniformf("dir", 0f, 1f)
        shader.setUniformf("resolution", original.height.toFloat() * 8f)
        //batch.begin()

        batch.draw(regionB, 0f, 0f)
        batch.flush()
        batch.end()

        if (original is SpriteScene) {
            Ghost.rayHandler.updateAndRender()
        }

        batch.shader = null
    }

    fun fadeIn(callback: Runnable?) {
        fading = true
        fadeStart = radius
        fadeStartTime = TimeUtils.millis()
        fadeEnd = maxRadius
        this.fadeCallback = callback
    }

    fun fadeOut(callback: Runnable?) {
        fading = true
        fadeStart = radius
        fadeEnd = 0.1f
        fadeStartTime = TimeUtils.millis()
        this.fadeCallback = callback
    }

    override fun dispose() {
        targetA.dispose()
        targetB.dispose()
    }

    override fun isVisible() : Boolean {
        return original.isVisible
    }
}
