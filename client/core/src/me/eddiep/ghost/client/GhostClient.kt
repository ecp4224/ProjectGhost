package me.eddiep.ghost.client;

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import me.eddiep.ghost.client.core.Blend
import me.eddiep.ghost.client.core.Drawable
import me.eddiep.ghost.client.core.Logical
import me.eddiep.ghost.client.core.Text
import java.util.*

class GhostClient(val handler : Handler) : ApplicationAdapter() {
    lateinit var batch : SpriteBatch; //We need to delay this
    var sprites: HashMap<Blend, ArrayList<Drawable>> = HashMap();
    var logicals : ArrayList<Logical?> = ArrayList();
    var loaded : Boolean = false;
    lateinit var camera : OrthographicCamera; //We need to delay this
    lateinit var progressBarBack : Sprite;
    lateinit var progressBarFront : Sprite;
    lateinit var progressText : Text

    private var isSpriteLooping: Boolean = false
    private var isLogicLooping: Boolean = false
    private var spritesToAdd: ArrayList<Drawable> = ArrayList()
    private var spritesToRemove: ArrayList<Drawable> = ArrayList()
    private var logicsToAdd: ArrayList<Logical?> = ArrayList()
    private var logicsToRemove: ArrayList<Logical?> = ArrayList()

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

            isLogicLooping = true
            logicals forEach { it?.tick() }
            isLogicLooping = false

            logicals.addAll(logicsToAdd)
            logicsToAdd.clear()
            logicsToRemove forEach { logicals.remove(it) }
            logicsToRemove.clear()

            camera.update()

            batch.projectionMatrix = camera.combined;
            batch.begin()

            isSpriteLooping = true
            for (blend in sprites.keySet()) {
                if (blend.isDifferent(batch)) {
                    blend.apply(batch)
                }

                val array: ArrayList<Drawable> = sprites.get(blend) ?: continue
                array forEach {
                    it.draw(batch)
                }
            }
            isSpriteLooping = false

            spritesToAdd forEach {
                if (sprites.containsKey(it.blendMode()))
                    sprites.get(it.blendMode())?.add(it)
                else {
                    val temp = ArrayList<Drawable>()
                    temp.add(it)
                    sprites.put(it.blendMode(), temp)
                }
            }

            spritesToAdd.clear()
            spritesToRemove forEach {
                if (sprites.containsKey(it.blendMode())) {
                    sprites.get(it.blendMode())?.remove(it)
                }
            }
            spritesToRemove.clear()

            batch.end()


        }
    }

    public fun addEntity(entity: Drawable) {
        if (isSpriteLooping)
            spritesToAdd.add(entity)
        else {
            if (sprites.containsKey(entity.blendMode()))
                sprites.get(entity.blendMode())?.add(entity)
            else {
                val temp = ArrayList<Drawable>()
                temp.add(entity)
                sprites.put(entity.blendMode(), temp)
            }
        }

        entity.load()

        if (entity is Logical) {
            addLogical(entity)
        }
    }

    public fun removeEntity(entity: Drawable) {
        if (isSpriteLooping)
            spritesToRemove.add(entity)
        else {
            if (sprites.containsKey(entity.blendMode())) {
                sprites.get(entity.blendMode())?.remove(entity)
            }
        }

        Gdx.app.postRunnable { entity.unload() }

        if (entity is Logical) {
            removeLogical(entity)
        }
    }

    public fun addLogical(logic: Logical) {
        if (isLogicLooping)
            logicsToAdd.add(logic)
        else
            logicals.add(logic)
    }

    public fun removeLogical(logic: Logical) {
        if (isLogicLooping)
            logicsToRemove.add(logic)
        else
            logicals.remove(logic)

        logic.dispose()
    }

    fun clearScreen() {
        sprites.clear()
    }
}