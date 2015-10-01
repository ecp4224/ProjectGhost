package me.eddiep.ghost.client;

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import me.eddiep.ghost.client.core.Drawable
import me.eddiep.ghost.client.core.Logical
import java.util.*

class GhostClient(val handler : Handler) : ApplicationAdapter() {
    lateinit var batch : SpriteBatch; //We need to delay this
    var sprites : ArrayList<Drawable> = ArrayList();
    var logicals : ArrayList<Logical> = ArrayList();
    lateinit var camera : OrthographicCamera; //We need to delay this

    override fun create() {
        batch = SpriteBatch()
        camera = OrthographicCamera(1024f, 720f)
        handler.start()

        camera.position.x = 512f
        camera.position.y = 360f

        //camera.zoom = -3f
    }

    override fun render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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

    private fun addSpriteSync(entity: Drawable) = sprites.add(entity);
    private fun addLogicalSync(logical: Logical) = logicals.add(logical);
    private fun removeSpriteSync(entity: Drawable) = sprites.remove(entity);
    private fun removeLogicalSync(logical: Logical) = logicals.remove(logical);

    public fun addEntity(entity: Drawable) {
        Gdx.app.postRunnable {
            entity.load()

            addSpriteSync(entity)

            if (entity is Logical) {
                addLogicalSync(entity)
            }
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