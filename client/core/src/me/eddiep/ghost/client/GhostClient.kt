package me.eddiep.ghost.client;

import box2dLight.RayHandler
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Matrix4
import me.eddiep.ghost.client.core.Blend
import me.eddiep.ghost.client.core.Drawable
import me.eddiep.ghost.client.core.Logical
import me.eddiep.ghost.client.core.Text
import java.util.*

class GhostClient(val handler : Handler) : ApplicationAdapter() {
    lateinit var batch : SpriteBatch; //We need to delay this
    var sprites: HashMap<Blend, ArrayList<Drawable>> = HashMap();
    var uiSprites: HashMap<Blend, ArrayList<Drawable>> = HashMap();
    var logicals : ArrayList<Logical?> = ArrayList();
    var loaded : Boolean = false;

    lateinit var camera : OrthographicCamera; //We need to delay this
    var normalProjection = Matrix4()
    lateinit var rayHandler : RayHandler;
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

        normalProjection.setToOrtho2D(0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat());

        Ghost.loadGameAssets(Ghost.ASSETS)

        //camera.zoom = -3f
    }

    override fun render() {
        try {
            _render()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    fun renderScene() {

        isSpriteLooping = true

        //Render all light sprites

        //Update and set the camera
        camera.update()

        batch.projectionMatrix = camera.combined;
        batch.begin()
        try {
            for (blend in sprites.keys) {
                if (blend.isDifferent(batch)) {
                    blend.apply(batch)
                }

                val array = sprites.get(blend) ?: continue
                for (sprite in array) {
                    sprite.draw(batch)
                }
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }

        batch.end()

        //Render UI sprites
        batch.projectionMatrix = normalProjection;
        batch.begin();

        try {
            for (blend in uiSprites.keys) {
                if (blend.isDifferent(batch)) {
                    blend.apply(batch)
                }

                val array = sprites.get(blend) ?: continue
                for (ui in array) {
                    ui.draw(batch)
                }
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }

        batch.end()

        isSpriteLooping = false

        updateSprites()
    }

    fun updateSprites() {
        for (toAdd in spritesToAdd) {
            val map = if (toAdd.hasLighting()) sprites else uiSprites

            if (map.containsKey(toAdd.blendMode()))
                map.get(toAdd.blendMode())?.add(toAdd)
            else {
                val temp = ArrayList<Drawable>()
                temp.add(toAdd)
                map.put(toAdd.blendMode(), temp)
            }
        }

        spritesToAdd.clear()

        for (toRemove in spritesToRemove) {
            if (sprites.containsKey(toRemove.blendMode())) {
                sprites.get(toRemove.blendMode())?.remove(toRemove)
            }
            if (uiSprites.containsKey(toRemove.blendMode())) {
                uiSprites.get(toRemove.blendMode())?.remove(toRemove)
            }
        }

        spritesToRemove.clear()
    }

    fun tick() {
        //Tick the current handler
        handler.tick()

        //Loop through any logic
        isLogicLooping = true
        logicals forEach { it?.tick() }
        isLogicLooping = false

        //Update logic array
        logicals.addAll(logicsToAdd)
        logicsToAdd.clear()
        logicsToRemove forEach { logicals.remove(it) }
        logicsToRemove.clear()
    }

    fun _renderLoading() {
        camera.update()

        var temp = Ghost.ASSETS.progress * 720f

        progressBarFront.setSize(temp, 16f)

        batch.projectionMatrix = camera.combined;
        batch.begin()

        progressText.draw(batch)
        progressBarBack.draw(batch)
        progressBarFront.draw(batch)

        batch.end()
    }

    fun _render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!loaded && Ghost.ASSETS.update()) { //If we are loading and the asset manager reports to be finished
            handler.start()
            loaded = true
        } else if (!loaded) { //If we are still loading
            _renderLoading()
        } else { //If we are done loading
            tick()

            renderScene()
        }
    }

    public fun addEntity(entity: Drawable) {
        if (isSpriteLooping)
            spritesToAdd.add(entity)
        else {
            val map = if (entity.hasLighting()) sprites else uiSprites

            if (map.containsKey(entity.blendMode()))
                map.get(entity.blendMode())?.add(entity)
            else {
                val temp = ArrayList<Drawable>()
                temp.add(entity)
                map.put(entity.blendMode(), temp)
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
            if (uiSprites.containsKey(entity.blendMode())) {
                uiSprites.get(entity.blendMode())?.remove(entity)
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
        Gdx.app.postRunnable {
            sprites.clear()
            uiSprites.clear()
            logicals.clear()
            spritesToRemove.clear()
            spritesToAdd.clear()
            logicsToAdd.clear()
            logicsToRemove.clear()
        }
    }
}