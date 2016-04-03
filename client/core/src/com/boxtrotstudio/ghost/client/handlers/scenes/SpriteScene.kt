package com.boxtrotstudio.ghost.client.handlers.scenes

import box2dLight.RayHandler
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.physics.box2d.Box2D
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.core.logic.Logical
import com.boxtrotstudio.ghost.client.core.render.Blend
import com.boxtrotstudio.ghost.client.core.render.Drawable
import com.boxtrotstudio.ghost.client.core.render.scene.AbstractScene
import java.util.*

public class SpriteScene : AbstractScene() {
    public lateinit var rayHandler : RayHandler;
    //private var sprites = ArrayList<Drawable>();
    //private var uiSprites = ArrayList<Drawable>();
    private var sprites: HashMap<Blend, ArrayList<Drawable>> = HashMap();
    private var uiSprites: HashMap<Blend, ArrayList<Drawable>> = HashMap();
    private var isSpriteLooping: Boolean = false
    private var spritesToAdd: ArrayList<Drawable> = ArrayList()
    private var spritesToRemove: ArrayList<Drawable> = ArrayList()
    private var normalProjection = Matrix4()
    private var dirty = false

    override fun init() {
        Box2D.init()

        rayHandler = RayHandler(Ghost.getInstance().world)
        rayHandler.setAmbientLight(0f, 0f, 0f, 0.5f);
        rayHandler.setBlurNum(3);

        normalProjection.setToOrtho2D(0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat());

        for (light in Ghost.lights) {
            light.createLight()
        }
    }

    override fun render(camera: OrthographicCamera, batch: SpriteBatch) {
        isSpriteLooping = true

        //Render all light sprites
        batch.begin()
        try {
            for (blend in sprites.keys) {
                if (blend.isDifferent(batch)) {
                    blend.apply(batch)
                }

                val array = sprites[blend] ?: continue
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
        val oldMatrix = batch.projectionMatrix
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

        batch.projectionMatrix = oldMatrix

        if (dirty) {
            sortSprites()
            dirty = false
        }

        isSpriteLooping = false

        updateSprites()
    }

    override fun dispose() {
        rayHandler.dispose()
    }

    fun updateSprites() {
        dirty = spritesToAdd.size > 0 || spritesToRemove.size > 0

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

        //sprites.addAll(spritesToAdd)

        spritesToAdd.clear()

        for (toRemove in spritesToRemove) {
            if (sprites.containsKey(toRemove.blendMode())) {
                sprites.get(toRemove.blendMode())?.remove(toRemove)
            }
            if (uiSprites.containsKey(toRemove.blendMode())) {
                uiSprites.get(toRemove.blendMode())?.remove(toRemove)
            }
        }

        //sprites.removeAll(spritesToRemove)

        spritesToRemove.clear()
    }

    public fun sortSprites() {
        for (b in sprites.keys) {
            Collections.sort(sprites[b], { o1, o2 -> o1.zIndex - o2.zIndex })
            //Collections.sort(sprites, { o1, o2 -> o1.zIndex - o2.zIndex })
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
            //sprites.add(entity)

            if (entity.hasLighting()) {
                dirty = true
            }
        }

        entity.parentScene = this
        entity.load()

        if (entity is Logical) {
            Ghost.getInstance().addLogical(entity)
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
            //sprites.remove(entity)
        }

        if (entity.hasLighting())
            dirty = true

        Gdx.app.postRunnable { entity.unload() }

        if (entity is Logical) {
            Ghost.getInstance().removeLogical(entity)
        }
    }

    fun clear() {
        sprites.clear()
        spritesToAdd.clear()
        spritesToRemove.clear()
        uiSprites.clear()
    }
}