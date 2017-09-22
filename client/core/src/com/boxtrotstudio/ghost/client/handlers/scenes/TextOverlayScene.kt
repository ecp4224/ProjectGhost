package com.boxtrotstudio.ghost.client.handlers.scenes

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.boxtrotstudio.ghost.client.core.game.Entity
import com.boxtrotstudio.ghost.client.core.game.SpriteEntity
import com.boxtrotstudio.ghost.client.core.game.sprites.FightBanner
import com.boxtrotstudio.ghost.client.core.render.Text
import com.boxtrotstudio.ghost.client.core.render.scene.AbstractScene
import com.boxtrotstudio.ghost.client.utils.Constants
import com.boxtrotstudio.ghost.client.utils.PFunction
import java.util.*

class TextOverlayScene(val header: String, val subtext: String, var showDots: Boolean) : AbstractScene() {
    private lateinit var headerText: Text
    private lateinit var subText: Text
    private var textToBanner = HashMap<String, Function0<Entity>>()
    private var entity : Entity? = null

    var dots = 0
    override fun onInit() {
        headerText = Text(36, Constants.Colors.PRIMARY, Gdx.files.internal("fonts/7thservice.ttf"));
        headerText.x = 640f
        headerText.y = 360f
        headerText.text = header
        headerText.load()

        subText = Text(28, Constants.Colors.PRIMARY, Gdx.files.internal("fonts/7thservicecond.ttf"));
        subText.x = 640f
        subText.y = 300f
        subText.text = subtext
        subText.load()

        requestOrder(-2)


        loadBanners()
    }

    private fun loadBanners() {
        //textToBanner.put("Fight!", { FightBanner() })
    }

    fun setHeaderText(text: String) {
        entity = null

        if (textToBanner.containsKey(text)) {
            entity = (textToBanner[text])?.invoke()
            entity?.load()
            headerText.text = ""
        } else {
            headerText.text = text
        }
    }


    public fun setSubText(text: String) {
        subText.text = text
    }

    private var lastDot = 0L
    override fun render(camera: OrthographicCamera, batch: SpriteBatch) {
        if (showDots) {
            if (System.currentTimeMillis() - lastDot > 800) {
                dots++;
                if (dots == 4)
                    dots = 0;


                subText.text = subtext
                for (i in 0..dots) {
                    subText.text += "."
                }

                subText.x = 640f

                lastDot = System.currentTimeMillis()
            }
        }

        batch.begin()

        headerText.draw(batch)
        subText.draw(batch)
        entity?.draw(batch)

        batch.end()
    }

    override fun dispose() {

    }
}
