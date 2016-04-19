package com.boxtrotstudio.ghost.client.handlers.scenes

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ScalingViewport
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.core.render.Text
import com.boxtrotstudio.ghost.client.core.render.scene.AbstractScene
import com.boxtrotstudio.ghost.client.utils.GlobalOptions

class OptionScene : AbstractScene() {
    private lateinit var header: Text;
    private lateinit var stage: Stage;

    override fun onInit() {
        val widthMult = (Gdx.graphics.width / 1280f)
        val heightMult = (Gdx.graphics.height / 720f)

        header = Text(72, Color.WHITE, Gdx.files.internal("fonts/INFO56_0.ttf"));
        header.x = 640f * widthMult
        header.y = 620f * heightMult
        header.text = "OPTIONS"
        header.load()

        stage = Stage(
                ScalingViewport(Scaling.stretch, 1280f, 720f, OrthographicCamera()),
                Ghost.getInstance().batch
        )
        Gdx.input.inputProcessor = stage

        val skin = Skin(Gdx.files.internal("sprites/ui/uiskin.json"))

        Ghost.setStage(stage, skin)

        var table = Table()
        table.width = 300f;
        table.height = 400f;
        table.x = 640f - (table.width / 2f)
        table.y = 400f - (table.height / 2f)
        stage.addActor(table)

        val masterVolumeText = Label("MASTER VOLUME:", skin)
        val masterVolume = Slider(0f, 1f, 0.01f, false, skin)
        val musicVolumeText = Label("MUSIC VOLUME:", skin)
        val musicVolume = Slider(0f, 1f, 0.01f, false, skin)
        val fxVolumeText = Label("FX VOLUME:", skin)
        val fxVolume = Slider(0f, 1f, 0.01f, false, skin)

        val resolutionText = Label("RESOLUTION:", skin)
        val resolution = SelectBox<String>(skin)
        resolution.items = GlobalOptions.getResolutions();

        val fullscreen = CheckBox("FULLSCREEN", skin)
        val fps = CheckBox("DISPLAY FPS", skin)
        val ping = CheckBox("DISPLAY PING", skin)

        masterVolume.value = GlobalOptions.getOptions().masterVolume();
        musicVolume.value = GlobalOptions.getOptions().musicVolume();
        fxVolume.value = GlobalOptions.getOptions().fxVolume();
        resolution.selectedIndex = GlobalOptions.getResolutions().indexOf(GlobalOptions.getOptions().resolution());
        fullscreen.isChecked = GlobalOptions.getOptions().fullscreen();
        fps.isChecked = GlobalOptions.getOptions().displayFPS();
        ping.isChecked = GlobalOptions.getOptions().displayPing()

        table.add(masterVolumeText).width(100f).height(40f)
        table.row()
        table.add().width(100f).height(0f)
        table.add(masterVolume).width(200f).height(40f)
        table.row()

        table.add(musicVolumeText).width(100f).height(40f)
        table.row()
        table.add().width(100f).height(0f)
        table.add(musicVolume).width(200f).height(40f)
        table.row()

        table.add(fxVolumeText).width(100f).height(40f)
        table.row()
        table.add().width(100f).height(0f)
        table.add(fxVolume).width(200f).height(40f)
        table.row()

        table.add(resolutionText).width(100f).height(40f)
        table.row()
        table.add().width(100f).height(0f)
        table.add(resolution).width(200f).height(40f)
        table.row()

        table.add().width(100f).height(0f)
        table.add(fullscreen).width(200f).height(40f)
        table.row()

        table.add().width(100f).height(0f)
        table.add(fps).width(200f).height(40f)
        table.row()

        table.add().width(100f).height(0f)
        table.add(ping).width(200f).height(40f)
        table.row()


        val buttonTable = Table()
        buttonTable.width = 600f;
        buttonTable.height = 40f;
        buttonTable.x = 640f - (buttonTable.width / 2f)
        buttonTable.y = 15f
        stage.addActor(buttonTable)

        val backButton = TextButton("BACK", skin)
        val applyButton = TextButton("APPLY", skin)
        buttonTable.add(applyButton).width(130f).height(40f)
        buttonTable.add(backButton).width(130f).height(40f).padLeft(20f)

        backButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                replaceWith(MenuScene())
            }
        })

        applyButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                GlobalOptions.getOptions().setMasterVolume(masterVolume.value)
                GlobalOptions.getOptions().setMusicVolume(musicVolume.value)
                GlobalOptions.getOptions().setFXVolume(fxVolume.value)
                GlobalOptions.getOptions().setDisplayFPS(fps.isChecked)
                GlobalOptions.getOptions().setDisplayPing(ping.isChecked)

                val changed = resolution.selected != GlobalOptions.getOptions().resolution() || fullscreen.isChecked != GlobalOptions.getOptions().fullscreen();

                GlobalOptions.getOptions().setResolution(resolution.selected)
                GlobalOptions.getOptions().setFullscreen(fullscreen.isChecked)

                if (changed) {
                    Ghost.createInfoDialog("Resolution Change", "For changes to take effect, you must restart the game.", null)
                }

                GlobalOptions.getOptions().save(GlobalOptions.getConfigLocation())
            }
        })
    }

    override fun render(camera: OrthographicCamera, batch: SpriteBatch) {
        batch.begin()
        header.draw(batch)
        batch.end()

        stage.act()
        stage.draw()
    }

    override fun dispose() {
        stage.dispose()
    }
}
