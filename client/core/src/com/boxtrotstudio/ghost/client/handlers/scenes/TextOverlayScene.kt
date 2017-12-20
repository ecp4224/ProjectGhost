package com.boxtrotstudio.ghost.client.handlers.scenes

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.boxtrotstudio.ghost.client.core.game.Entity
import com.boxtrotstudio.ghost.client.core.render.Text
import com.boxtrotstudio.ghost.client.core.render.scene.AbstractScene
import com.boxtrotstudio.ghost.client.utils.Constants
import java.util.*

class TextOverlayScene(val header: String, var subtext: String, var showDots: Boolean) : AbstractScene() {
    private lateinit var headerTextUI: Text
    private lateinit var subTextUI: Text
    private var textToBanner = HashMap<String, Function0<Entity>>()
    private var entity : Entity? = null

    var dots = 0
    override fun onInit() {
        headerTextUI = Text(36, Constants.Colors.PRIMARY, Gdx.files.internal("fonts/7thservice.ttf"));
        headerTextUI.x = 640f
        headerTextUI.y = 360f
        headerTextUI.text = header
        headerTextUI.load()

        subTextUI = Text(28, Constants.Colors.PRIMARY, Gdx.files.internal("fonts/7thservicecond.ttf"));
        subTextUI.x = 640f
        subTextUI.y = 300f
        subTextUI.text = subtext
        subTextUI.load()

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
            headerTextUI.text = ""
        } else {
            headerTextUI.text = text
        }
    }


    public fun setSubText(text: String) {
        subtext = text
        subTextUI.text = text
    }

    private var lastDot = 0L
    override fun render(camera: OrthographicCamera, batch: SpriteBatch) {
        if (showDots) {
            if (System.currentTimeMillis() - lastDot > 800) {
                dots++;
                if (dots == 4)
                    dots = 0;


                subTextUI.text = subtext
                for (i in 0..dots) {
                    subTextUI.text += "."
                }

                subTextUI.x = 640f

                lastDot = System.currentTimeMillis()
            }
        }

        batch.begin()

        headerTextUI.draw(batch)
        subTextUI.draw(batch)
        entity?.draw(batch)

        batch.end()
    }

    override fun dispose() {

    }
}
