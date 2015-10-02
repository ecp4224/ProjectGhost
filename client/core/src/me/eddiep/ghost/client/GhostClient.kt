package me.eddiep.ghost.client;

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import me.eddiep.ghost.client.core.Drawable
import me.eddiep.ghost.client.core.Logical
import me.eddiep.ghost.client.core.Text
import java.util.*

class GhostClient(val handler : Handler) : ApplicationAdapter() {
    lateinit var batch : SpriteBatch; //We need to delay this
    var sprites : ArrayList<Drawable> = ArrayList();
    var logicals : ArrayList<Logical> = ArrayList();
    var loaded : Boolean = false;
    lateinit var camera : OrthographicCamera; //We need to delay this
    lateinit var progressBarBack : Sprite;
    lateinit var progressBarFront : Sprite;
    lateinit var progressText : Text

    override fun create() {
        var back = Texture("sprites/progress_back.png")
        var front = Texture("sprites/progress_front.png");

        progressBarBack = Sprite(back)
        progressBarFront = Sprite(front)

        progressBarFront.setCenter(512f, 32f)
        progressBarBack.setCenter(512f, 32f)

        progressBarFront.setOriginCenter()
        progressBarBack.setOriginCenter()

        progressText = Text(36, Color.WHITE, Gdx.files.internal("fonts/INFO56_0.ttf"));
        progressText.x = 512f
        progressText.y = 360f
        progressText.text = "LOADING..."
        progressText.load()

        batch = SpriteBatch()
        camera = OrthographicCamera(1024f, 720f)
        camera.setToOrtho(false, 1024f, 720f)

        Ghost.loadGameAssets(Ghost.ASSETS)

        //camera.zoom = -3f
    }

    override fun render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!loaded && Ghost.ASSETS.update()) {
            handler.start()
            loaded = true
        } else if (!loaded) {
            camera.update()

            var temp = Ghost.ASSETS.progress * 720f

            progressBarFront.setSize(temp, 16f)

            batch.projectionMatrix = camera.combined;
            batch.begin()

            progressText.draw(batch)
            progressBarBack.draw(batch)
            progressBarFront.draw(batch)

            batch.end()
        } else {
            handler.tick()

            synchronized(logicals, {
                logicals forEach { it.tick() }
            })

            camera.update()

            batch.projectionMatrix = camera.combined;
            batch.begin()

            synchronized(sprites, {
                sprites forEach { it.draw(batch) }
            })

            batch.end()
        }
    }

    private fun addSpriteSync(entity: Drawable) = sprites.add(entity);
    private fun addLogicalSync(logical: Logical) = logicals.add(logical);
    private fun removeSpriteSync(entity: Drawable) = sprites.remove(entity);
    private fun removeLogicalSync(logical: Logical) = logicals.remove(logical);

    public fun addEntity(entity: Drawable) {
        synchronized(sprites, {
            addSpriteSync(entity)
        })

        entity.load()

        if (entity is Logical) {
            addLogicalSync(entity)
        }
    }

    public fun removeEntity(entity: Drawable) {
        synchronized(sprites, {
            removeSpriteSync(entity)
        })

        Gdx.app.postRunnable { entity.unload() }

        if (entity is Logical) {
            synchronized(logicals, {
                removeLogicalSync(entity);
            })
        }
    }
}