package com.boxtrotstudio.ghost.client.handlers.scenes.builder

import box2dLight.ConeLight
import box2dLight.Light
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.core.render.scene.AbstractScene
import com.boxtrotstudio.ghost.client.handlers.LightBuildHandler

class BuilderOverlayScene(val handler: LightBuildHandler) : AbstractScene() {
    private lateinit var stage: Stage;
    private lateinit var ambiantSlider: Slider;
    private lateinit var xPos: TextField
    private lateinit var yPos: TextField
    private lateinit var color: TextField
    private lateinit var intensity: TextField
    private lateinit var distance: TextField
    private lateinit var direction: TextField
    private lateinit var degrees: TextField
    private lateinit var isSoft: CheckBox

    override fun onInit() {
        stage = Stage(
                Ghost.getInstance().viewport,
                Ghost.getInstance().batch
        )
        Gdx.input.inputProcessor = stage

        val skin = Skin(Gdx.files.internal("sprites/ui/uiskin.json"))

        Ghost.setStage(stage, skin)

        val table2 = Table()
        table2.x = 1050f
        table2.y = 550f
        table2.width = 200f
        table2.height = 100f

        val labelX = Label("X: ", skin)
        val labelY = Label("Y: ", skin)
        val labelColor = Label("Color: ", skin)
        val labelIntensity = Label("Intensity: ", skin)
        val labelDistance = Label("Distance: ", skin)
        val labelDirection = Label("Direction: ", skin)
        val labelDegrees = Label("Degrees: ", skin)

        xPos = TextField("", skin)
        yPos = TextField("", skin)
        color = TextField("", skin)
        intensity = TextField("", skin)
        distance = TextField("", skin)
        isSoft = CheckBox("Is Soft?", skin)
        direction = TextField("", skin)
        degrees = TextField("", skin)

        table2.add(labelX).width(50f)
        table2.add(xPos).width(100f)
        table2.row()
        table2.add(labelY).width(50f)
        table2.add(yPos).width(100f)
        table2.row()
        table2.add(labelColor).width(50f)
        table2.add(color).width(100f)
        table2.row()
        table2.add(labelIntensity).width(50f)
        table2.add(intensity).width(100f)
        table2.row()
        table2.add(labelDistance).width(50f)
        table2.add(distance).width(100f)
        table2.row()
        table2.add(labelDirection).width(50f)
        table2.add(direction).width(100f)
        table2.row()
        table2.add(labelDegrees).width(50f)
        table2.add(degrees).width(100f)
        table2.row()
        table2.add(isSoft).width(100f)

        stage.addActor(table2)

        val table = Table()
        table.x = 950f
        table.y = 10f
        table.width = 300f
        table.height = 100f

        val lable = Label("Ambient Light: ", skin)
        table.add(lable)

        ambiantSlider = Slider(0f, 1f, 0.01f, false, skin)
        ambiantSlider.value = 0.4f
        table.add(ambiantSlider).width(200f).padLeft(10f)
        table.row()

        val addLight = TextButton("Add Point Light", skin)
        val addLight2 = TextButton("Add Cone Light", skin)
        table.add(addLight).width(130f).height(40f).padTop(10f)
        table.add(addLight2).width(130f).height(40f).padTop(10f)

        addLight.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                handler.addPointLight()
            }
        })

        addLight2.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                handler.addConeLight()
            }
        })

        stage.addActor(table)

        setupHandlers()
    }

    override fun render(camera: OrthographicCamera, batch: SpriteBatch) {
        stage.draw()
        stage.act()

        Ghost.rayHandler.setAmbientLight(ambiantSlider.value, ambiantSlider.value, ambiantSlider.value, 1f)

        if (handler.lastCurrentLight != null)
            handler.lastCurrentLight?.isSoft = isSoft.isChecked
    }

    override fun dispose() {
        stage.dispose()
    }

    public fun updateInfo(cur: Light) {
        xPos.text = cur.x.toString()
        yPos.text = cur.y.toString()
        distance.text = cur.distance.toString()

        var rgb = cur.color.toString()
        if (rgb.length == 8)
            rgb = rgb.substring(0, 5)

        color.text = rgb
        intensity.text = cur.color.a.toString()

        isSoft.isChecked = cur.isSoft
        direction.text = cur.direction.toString()
        if (cur is ConeLight)
            degrees.text = cur.coneDegree.toString()
        else
            degrees.text = "N/A"
    }

    private fun setupHandlers() {
        xPos.setTextFieldListener(TextField.TextFieldListener { textField, key ->
            if (handler.lastCurrentLight == null)
                return@TextFieldListener

            val cur = handler.lastCurrentLight as Light

            try {
                val value = xPos.text.toFloat()
                cur.setPosition(value, cur.y)
            } catch (ex: NumberFormatException) {
                return@TextFieldListener
            }
        })

        yPos.setTextFieldListener(TextField.TextFieldListener { textField, key ->
            if (handler.lastCurrentLight == null)
                return@TextFieldListener

            val cur = handler.lastCurrentLight as Light

            try {
                val value = yPos.text.toFloat()
                cur.setPosition(cur.x, value)
            } catch (ex: NumberFormatException) {
                return@TextFieldListener
            }
        })

        distance.setTextFieldListener(TextField.TextFieldListener { textField, key ->
            if (handler.lastCurrentLight == null)
                return@TextFieldListener

            val cur = handler.lastCurrentLight as Light

            try {
                val value = distance.text.toFloat()
                cur.distance = value
            } catch (ex: NumberFormatException) {
                return@TextFieldListener
            }
        })

        color.setTextFieldListener(TextField.TextFieldListener { textField, key ->
            if (handler.lastCurrentLight == null)
                return@TextFieldListener

            val cur = handler.lastCurrentLight as Light

            try {
                var txt = color.text
                if (txt.startsWith("#"))
                    txt = txt.substring(1)

                val color = Color.valueOf(txt)
                cur.color = color
            } catch (ex: NumberFormatException) {
                return@TextFieldListener
            } catch (ex2: StringIndexOutOfBoundsException) {
                return@TextFieldListener
            }
        })

        intensity.setTextFieldListener(TextField.TextFieldListener { textField, key ->
            if (handler.lastCurrentLight == null)
                return@TextFieldListener

            val cur = handler.lastCurrentLight as Light

            try {
                val alpha = intensity.text.toFloat()
                val color = Color(cur.color.r, cur.color.g, cur.color.b, alpha)
                cur.color = color
            } catch (ex: NumberFormatException) {
                return@TextFieldListener
            } catch (ex2: StringIndexOutOfBoundsException) {
                return@TextFieldListener
            }
        })

        direction.setTextFieldListener(TextField.TextFieldListener { textField, key ->
            if (handler.lastCurrentLight == null)
                return@TextFieldListener

            val cur = handler.lastCurrentLight as Light

            try {
                val dir = direction.text.toFloat()
                cur.setDirection(dir)
            } catch (ex: NumberFormatException) {
                return@TextFieldListener
            } catch (ex2: StringIndexOutOfBoundsException) {
                return@TextFieldListener
            }
        })

        degrees.setTextFieldListener(TextField.TextFieldListener { textField, key ->
            if (handler.lastCurrentLight == null)
                return@TextFieldListener
            if (handler.lastCurrentLight !is ConeLight)
                return@TextFieldListener

            val cur = handler.lastCurrentLight as ConeLight

            try {
                val de = degrees.text.toFloat()
                cur.coneDegree = de
            } catch (ex: NumberFormatException) {
                return@TextFieldListener
            } catch (ex2: StringIndexOutOfBoundsException) {
                return@TextFieldListener
            }
        })
    }
}