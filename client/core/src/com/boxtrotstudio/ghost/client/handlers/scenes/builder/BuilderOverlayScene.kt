package com.boxtrotstudio.ghost.client.handlers.scenes.builder

import box2dLight.ConeLight
import box2dLight.Light
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.core.game.SpriteEntity
import com.boxtrotstudio.ghost.client.core.render.scene.AbstractScene
import com.boxtrotstudio.ghost.client.handlers.LightBuildHandler
import com.boxtrotstudio.ghost.client.utils.GlobalOptions
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.widget.file.FileChooser
import com.kotcrab.vis.ui.widget.file.SingleFileChooserListener
import java.io.FileFilter

class BuilderOverlayScene(val handler: LightBuildHandler) : AbstractScene() {
    private lateinit var stage: Stage
    private lateinit var ambientSlider: Slider

    /*
    Light Properties
     */
    private lateinit var xPos: TextField
    private lateinit var yPos: TextField
    private lateinit var color: TextField
    private lateinit var intensity: TextField
    private lateinit var distance: TextField
    private lateinit var direction: TextField
    private lateinit var degrees: TextField
    private lateinit var isSoft: CheckBox
    private lateinit var labelX: Label
    private lateinit var labelY: Label
    private lateinit var labelColor: Label
    private lateinit var labelIntensity: Label
    private lateinit var labelDistance: Label
    private lateinit var labelDegrees: Label
    private lateinit var labelDirection: Label

    /*
    Entity Properties
     */
    private lateinit var exPos: TextField
    private lateinit var eyPos: TextField
    private lateinit var ezPos: TextField
    private lateinit var eScaleX: TextField
    private lateinit var eScaleY: TextField
    private lateinit var eRotation: TextField
    private lateinit var eLighting: CheckBox
    private lateinit var eLabelX: Label
    private lateinit var eLabelY: Label
    private lateinit var eLabelZ: Label
    private lateinit var eLabelScaleX: Label
    private lateinit var eLabelScaleY: Label
    private lateinit var eLabelRotation: Label

    /*
    UI
     */
    private lateinit var lockLights: CheckBox

    override fun onInit() {
        stage = Stage(
                Ghost.getInstance().viewport,
                Ghost.getInstance().batch
        )
        attachStage(stage)

        val skin = Skin(Gdx.files.internal("sprites/ui/uiskin.json"))

        Ghost.setStage(stage, skin)

        val table2 = Table()
        table2.x = 1050f
        table2.y = 550f
        table2.width = 200f
        table2.height = 100f

        labelX = Label("X: ", skin)
        labelY = Label("Y: ", skin)
        labelColor = Label("Color: ", skin)
        labelIntensity = Label("Intensity: ", skin)
        labelDistance = Label("Distance: ", skin)
        labelDirection = Label("Direction: ", skin)
        labelDegrees = Label("Degrees: ", skin)

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

        val table3 = Table()
        table3.x = 1050f
        table3.y = 550f
        table3.width = 200f
        table3.height = 100f

        eLabelX = Label("X: ", skin)
        eLabelY = Label("Y: ", skin)
        eLabelZ = Label("Z: ", skin)
        eLabelScaleX = Label("Scale X: ", skin)
        eLabelScaleY = Label("Scale Y: ", skin)
        eLabelRotation = Label("Rotation: ", skin)

        exPos = TextField("", skin)
        eyPos = TextField("", skin)
        ezPos = TextField("", skin)
        eScaleX = TextField("", skin)
        eScaleY = TextField("", skin)
        eRotation = TextField("", skin)
        eLighting = CheckBox("Lighting Enabled?", skin)
        eLighting.isChecked = true

        table3.add(eLabelX).width(50f)
        table3.add(exPos).width(100f)
        table3.row()
        table3.add(eLabelY).width(50f)
        table3.add(eyPos).width(100f)
        table3.row()
        table3.add(eLabelZ).width(50f)
        table3.add(ezPos).width(100f)
        table3.row()
        table3.add(eLabelScaleX).width(50f)
        table3.add(eScaleX).width(100f)
        table3.row()
        table3.add(eLabelScaleY).width(50f)
        table3.add(eScaleY).width(100f)
        table3.row()
        table3.add(eLabelRotation).width(50f)
        table3.add(eRotation).width(100f)
        table3.row()
        table3.add(eLighting).width(150f)

        stage.addActor(table3)

        val table = Table()
        table.x = 950f
        table.y = 10f
        table.width = 300f
        table.height = 100f

        val label = Label("Ambient Light: ", skin)
        table.add(label)

        ambientSlider = Slider(0f, 1f, 0.01f, false, skin)
        ambientSlider.value = 0.4f
        table.add(ambientSlider).width(200f).padLeft(10f)
        table.row()

        val addLight = TextButton("Add Point Light", skin)
        val addLight2 = TextButton("Add Cone Light", skin)
        lockLights = CheckBox("Lock Lights", skin)
        table.add(addLight).width(130f).height(40f).padTop(10f)
        table.add(addLight2).width(130f).height(40f).padTop(10f)

        lockLights.x = 950f
        lockLights.y = 10f


        stage.addActor(lockLights)

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

        GlobalOptions.getOptions().setDisplayPing(false)

        val addWall = TextButton("Add Wall", skin)
        addWall.x = 160f
        addWall.y = 20f
        addWall.height = 40f
        addWall.width = 130f
        addWall.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent? , x: Float, y: Float) {
                handler.addWall()
            }
        })

        val addImage = TextButton("Add Image", skin)
        addImage.y = 20f
        addImage.x = 20f
        addImage.width = 130f
        addImage.height = 40f

        addImage.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                val chooser = FileChooser(FileChooser.Mode.OPEN)
                chooser.fileFilter = FileFilter { f ->
                    f.isDirectory || f.name.endsWith("png") || f.name.endsWith(".jpg") || f.name.endsWith(".jpeg")
                }
                chooser.isMultiSelectionEnabled = false
                chooser.name = "Image Selection"

                chooser.setListener(object : SingleFileChooserListener() {
                    override fun selected(file: FileHandle?) {
                        if (file == null)
                            return
                        handler.addImage(file)
                    }
                })

                stage.addActor(chooser.fadeIn())
            }
        })

        stage.addActor(addImage)
        stage.addActor(addWall)

        setupHandlers()

        handler.started = true

        VisUI.load()
    }

    override fun render(camera: OrthographicCamera, batch: SpriteBatch) {
        stage.draw()
        stage.act()

        Ghost.rayHandler.setAmbientLight(ambientSlider.value, ambientSlider.value, ambientSlider.value, 1f)

        if (handler.lastCurrentLight != null)
            handler.lastCurrentLight?.isSoft = isSoft.isChecked

        if (handler.lastCurrentEntity != null)
            handler.lastCurrentEntity?.setHasLighting(eLighting.isChecked)

        handler.lockLights = lockLights.isChecked
    }

    override fun dispose() {
        stage.dispose()
    }

    public fun showConeLightProperties() {
        showPointLightProperties()
        direction.isVisible = true
        labelDirection.isVisible = true
        degrees.isVisible = true
        labelDegrees.isVisible = true
    }

    public fun showPointLightProperties() {
        xPos.isVisible = true
        labelX.isVisible = true
        yPos.isVisible = true
        labelY.isVisible = true
        distance.isVisible = true
        labelDistance.isVisible = true
        color.isVisible = true
        labelColor.isVisible = true
        intensity.isVisible = true
        labelIntensity.isVisible = true
        isSoft.isVisible = true
        direction.isVisible = false
        labelDirection.isVisible = false
        degrees.isVisible = false
        labelDegrees.isVisible = false
    }

    public fun hideLightProperties() {
        xPos.isVisible = false
        labelX.isVisible = false
        yPos.isVisible = false
        labelY.isVisible = false
        distance.isVisible = false
        labelDistance.isVisible = false
        color.isVisible = false
        labelColor.isVisible = false
        intensity.isVisible = false
        labelIntensity.isVisible = false
        isSoft.isVisible = false
        direction.isVisible = false
        labelDirection.isVisible = false
        degrees.isVisible = false
        labelDegrees.isVisible = false
    }

    public fun setVisibleEntityProperties(value: Boolean) {
        exPos.isVisible = value
        eyPos.isVisible = value
        ezPos.isVisible = value
        eLabelX.isVisible = value
        eLabelY.isVisible = value
        eLabelZ.isVisible = value
        eScaleX.isVisible = value
        eScaleY.isVisible = value
        eLabelScaleX.isVisible = value
        eLabelScaleY.isVisible = value
        eRotation.isVisible = value
        eLabelRotation.isVisible = value
        eLighting.isVisible = value
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

    public fun updateEntityInfo(cur: SpriteEntity) {
        exPos.text = cur.x.toString()
        eyPos.text = cur.y.toString()
        ezPos.text = cur.z.toString()
        eScaleX.text = cur.scaleX.toString()
        eScaleY.text = cur.scaleY.toString()
        eRotation.text = cur.rotation.toString()
    }

    private fun setupHandlers() {
        /*
        Light Handlers
         */

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

        /*
        Entity Handlers
         */
        exPos.setTextFieldListener(TextField.TextFieldListener { textField, key ->
            if (handler.lastCurrentEntity == null)
                return@TextFieldListener

            val cur = handler.lastCurrentEntity as SpriteEntity

            try {
                val value = exPos.text.toFloat()
                cur.x = value
            } catch (ex: NumberFormatException) {
                return@TextFieldListener
            }
        })

        eyPos.setTextFieldListener(TextField.TextFieldListener { textField, key ->
            if (handler.lastCurrentEntity == null)
                return@TextFieldListener

            val cur = handler.lastCurrentEntity as SpriteEntity

            try {
                val value = eyPos.text.toFloat()
                cur.y = value
            } catch (ex: NumberFormatException) {
                return@TextFieldListener
            }
        })

        ezPos.setTextFieldListener(TextField.TextFieldListener { textField, key ->
            if (handler.lastCurrentEntity == null)
                return@TextFieldListener

            val cur = handler.lastCurrentEntity as SpriteEntity

            try {
                val value = ezPos.text.toInt()
                cur.zIndex = value
            } catch (ex: NumberFormatException) {
                return@TextFieldListener
            }
        })

        eScaleX.setTextFieldListener(TextField.TextFieldListener { textField, key ->
            if (handler.lastCurrentEntity == null)
                return@TextFieldListener

            val cur = handler.lastCurrentEntity as SpriteEntity

            try {
                val value = eScaleX.text.toFloat()
                cur.setScale(value, cur.scaleY)
            } catch (ex: NumberFormatException) {
                return@TextFieldListener
            }
        })

        eScaleY.setTextFieldListener(TextField.TextFieldListener { textField, key ->
            if (handler.lastCurrentEntity == null)
                return@TextFieldListener

            val cur = handler.lastCurrentEntity as SpriteEntity

            try {
                val value = eScaleY.text.toFloat()
                cur.setScale(cur.scaleX, value)
            } catch (ex: NumberFormatException) {
                return@TextFieldListener
            }
        })

        eRotation.setTextFieldListener(TextField.TextFieldListener { textField, key ->
            if (handler.lastCurrentEntity == null)
                return@TextFieldListener

            val cur = handler.lastCurrentEntity as SpriteEntity

            try {
                val value = eRotation.text.toFloat()
                cur.rotation = value
            } catch (ex: NumberFormatException) {
                return@TextFieldListener
            }
        })
    }

    var isSaving = false
    fun openSaveDialog() {
        if (isSaving)
            return

        val chooser = FileChooser(FileChooser.Mode.SAVE)
        chooser.fileFilter = FileFilter { f ->
            f.isDirectory || f.name.endsWith("json")
        }
        chooser.isMultiSelectionEnabled = false
        chooser.name = "Save Location"

        chooser.setListener(object : SingleFileChooserListener() {
            override fun selected(file: FileHandle?) {
                if (file == null)
                    return
                handler.saveTo(file)
                isSaving = false
            }

            override fun canceled() {
                isSaving = false
            }

        })

        stage.addActor(chooser.fadeIn())
        isSaving = true
    }
}