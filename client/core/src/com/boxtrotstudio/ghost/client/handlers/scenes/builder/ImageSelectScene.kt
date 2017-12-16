package com.boxtrotstudio.ghost.client.handlers.scenes.builder

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.List
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.core.render.Text
import com.boxtrotstudio.ghost.client.core.render.scene.AbstractScene
import com.boxtrotstudio.ghost.client.handlers.LightBuildHandler

class ImageSelectScene(val handler: LightBuildHandler) : AbstractScene() {
    private lateinit var header: Text
    private lateinit var stage: Stage
    private lateinit var list: List<String>

    override fun onInit() {
        header = Text(72, Color.WHITE, Gdx.files.internal("fonts/TitilliumWeb-SemiBold.ttf"))
        header.x = 640f
        header.y = 520f
        header.text = "Background\nSelect"
        header.load()

        stage = Stage(
                Ghost.getInstance().viewport,
                Ghost.getInstance().batch
        )
        attachStage(stage)

        val skin = Skin(Gdx.files.internal("sprites/ui/uiskin.json"))

        Ghost.setStage(stage, skin)
        var table = Table()
        table.width = 200f
        table.height = 300f
        table.x = 640f - (table.width / 2f)
        table.y = 250f - (table.height / 2f)
        stage.addActor(table)

        list = List(skin)

        table.add(list).width(300f).height(200f).padBottom(10f)
        table.row()

        val button = TextButton("Select and Load", skin)
        table.add(button).width(150f).height(40f)

        button.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                handler.startBuilder(list.selected)
            }
        })
    }

    override fun render(camera: OrthographicCamera, batch: SpriteBatch) {
        updateList()

        batch.begin()
        header.draw(batch)
        batch.end()

        stage.act()
        stage.draw()
    }

    override fun dispose() {
        stage.dispose()
    }

    private fun updateList() {
        list.items.clear()

        val map_files = Gdx.files.internal("maps").list { pathname ->
            pathname.name.endsWith("png") ||
                    pathname.name.endsWith("PNG") ||
                    pathname.name.endsWith("jpg") ||
                    pathname.name.endsWith("JPG") ||
                    pathname.name.endsWith("json")
        }

        for (file in map_files) {
            list.items.add(file.path())
        }
    }
}