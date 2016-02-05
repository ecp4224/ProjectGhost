package me.eddiep.ghost.client;

import box2dLight.RayHandler
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Box2D
import com.badlogic.gdx.physics.box2d.World
import me.eddiep.ghost.client.core.logic.Handler
import me.eddiep.ghost.client.core.logic.LogicHandler
import me.eddiep.ghost.client.core.render.Blend
import me.eddiep.ghost.client.core.render.Drawable
import me.eddiep.ghost.client.core.logic.Logical
import me.eddiep.ghost.client.core.render.Text
import java.util.*

class GhostClient(val handler : Handler) : ApplicationAdapter() {
    private lateinit var batch : SpriteBatch; //We need to delay this
    private var sprites: HashMap<Blend, ArrayList<Drawable>> = HashMap();
    private var uiSprites: HashMap<Blend, ArrayList<Drawable>> = HashMap();
    private var loaded : Boolean = false;

    lateinit var camera : OrthographicCamera; //We need to delay this
    private var normalProjection = Matrix4()
    private lateinit var progressBarBack : Sprite;
    private lateinit var progressBarFront : Sprite;
    private lateinit var progressText : Text


    public lateinit var rayHandler : RayHandler;
    public lateinit var world : World;

    private var isSpriteLooping: Boolean = false
    private var spritesToAdd: ArrayList<Drawable> = ArrayList()
    private var spritesToRemove: ArrayList<Drawable> = ArrayList()

    private val logicalHandler = LogicHandler()

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

        Box2D.init()
        logicalHandler.init()

        world = World(Vector2(0f, 0f), true)
        rayHandler = RayHandler(world)
        rayHandler.setAmbientLight(0f, 0f, 0f, 0.5f);
        rayHandler.setBlurNum(3);

        Ghost.loadGameAssets(Ghost.ASSETS)
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

        rayHandler.setCombinedMatrix(camera)
        rayHandler.updateAndRender()

        //Render UI sprites
        batch.projectionMatrix = normalProjection;
        batch.begin();

        try {
            for (blend in uiSprites.keys) {
                if (blend.isDifferent(batch)) {
                    blend.apply(batch)
                }

                val array = uiSprites.get(blend) ?: continue
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
        val shouldSort = spritesToAdd.size > 0 || spritesToRemove.size > 0

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

        if (shouldSort)
            sortSprites()
    }

    private var accumulator = 0f
    fun doPhysicsStep(deltaTime: Float) {

        var frameTime = Math.min(deltaTime, 0.25f)
        accumulator += frameTime
        while (accumulator >= 0.01f) {
            world.step(0.01f, 6, 2)
            accumulator += 0.01f
        }
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
            logicalHandler.tick(handler, world)

            renderScene()
        }
    }

    public fun sortSprites() {
        for (b in sprites.keys) {
            Collections.sort(sprites[b], { o1, o2 -> o2.zIndex - o1.zIndex })
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

            if (entity.hasLighting()) {
                sortSprites()
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
        logicalHandler.addLogical(logic)
    }

    public fun removeLogical(logic: Logical) {
        logicalHandler.removeLogical(logic)
    }

    fun clearScreen() {
        Gdx.app.postRunnable {
            sprites.clear()
            uiSprites.clear()
            spritesToRemove.clear()
            spritesToAdd.clear()
            logicalHandler.clear()
        }
    }

    override fun dispose() {
        rayHandler.dispose()
        world.dispose()
    }
}
